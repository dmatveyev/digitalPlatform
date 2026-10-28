# План: тесты и рефакторинг `RatingParametersController.getAll`

Файлы:
- `src/main/java/com/example/digitalplatform/controller/RatingParametersController.java:29-44` — метод `getAll`.
- `src/test/java/com/example/digitalplatform/controller/RatingParametersControllerGetAllTest.java` — существующий тест (один happy path).

## 1. Анализ текущего метода

Логика: параметры `page` (по умолчанию 1) и `size` (по умолчанию 5), затем
`findAll()` → ручной `subList(start-1, min((start-1+pageSize), size))` → обёртка в `PageImpl`.

Проблемы:
- **N+1 / O(n)**: `findAll()` грузит все записи в память, хотя нужен только один page. При росте таблицы — деградация производительности и памяти.
- **Нет сортировки**: `PageRequest.of(start-1, pageSize)` без `Sort`. Порядок зависит от SQL/БД (обычно по PK), но это неявно; при пагинации страницы могут «прыгать» между запросами.
- **Нет валидации параметров**: `page=0` или отрицательные значения → `PageRequest.of(-1, n)` выбрасывает `IllegalArgumentException` (500); `size=-1`/`0` — тоже 500; нечисловые `page`/`size` → `TypeMismatchException`.
- **Ручная пагинация вместо repository**: `subList` + ручная формула `end = Math.min(...)` легко ломается (ошибки off-by-one, лишние проверки), хотя `JpaRepository.findAll(Pageable)` делает это нативно.
- **Возврат `Page<RatingParameters>` в model** — сам по себе ок для Thymeleaf, но сейчас строится «вручную».

## 2. Решение: рефакторинг обязателен (да/нет)

**Да, нужен.** Минимальный безопасный рефакторинг без изменения поведения контракта
(тот же view name, тот же model attribute `pageItem`, те же URL-параметры):

```java
@GetMapping("/all")
public String getAll(@RequestParam(name = "page", defaultValue = "1") int page,
                     @RequestParam(name = "size", defaultValue = "5") int size,
                     Model model) {
    if (page < 1 || size <= 0) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination parameters");
    }
    Page<RatingParameters> pageItem = ratingParametersRepository.findAll(
            PageRequest.of(page - 1, size, Sort.by("code")));
    model.addAttribute("pageItem", pageItem);
    return "rating_parameters/rating_parameters";
}
```

Дополнительно:
- Убрать неиспользуемые импорты (`Collections`, `Optional`).
- Поведение при невалидных параметрах: 400 (через `ResponseStatusException`) вместо 500. Это изменение контракта — зафиксировано в шаге 3.3.
- Сортировка по стабильному полю (`code` уникальный, либо `id`) — фиксируется тестом.

Альтернатива без изменения кода (out of scope): оставить как есть и покрыть текущее поведение тестами — не рекомендуется из-за O(n) и отсутствия сортировки.

## 3. Шаги

### Шаг 1. Рефакторинг `getAll` ✅ ВЫПОЛНЕН
1.1. Заменить `findAll()` + `subList` на `ratingParametersRepository.findAll(PageRequest.of(page - 1, size, Sort.by(...)))`.
1.2. Добавить проверку `page < 1 || size <= 0` → `ResponseStatusException(BAD_REQUEST)` (аналогично `create`).
1.3. Зафиксировать сортировку: `Sort.by("code")` (решено; code уникален по бизнесу и проверяется при `create`).
1.4. Убрать неиспользуемые импорты (`java.util.Collections`, `java.util.Optional`) и добавить `org.springframework.data.domain.Sort`.

**Саммари:** Метод `getAll` переписан: параметры `page`/`size` теперь принимаются через `@RequestParam(defaultValue = ...)` (int), добавлена валидация (`page < 1 || size <= 0` → `ResponseStatusException(HttpStatus.BAD_REQUEST)`, т.е. 400 вместо прежних 500). Пагинация выполняется нативно через `ratingParametersRepository.findAll(PageRequest.of(page - 1, size, Sort.by("code")))` — ручной `subList`/`PageImpl` удалён. Убраны неиспользуемые импорты (`Collections`, `Optional`, `PageImpl`), добавлен импорт `Sort`. Контракт сохранён: те же URL-параметры и значения по умолчанию (1 / 5), model attribute `"pageItem"` типа `Page<RatingParameters>`, view name `rating_parameters/rating_parameters`. Изменения поведения: детерминированная сортировка по `code` и статус 400 (вместо 500) при невалидных параметрах пагинации.

### Шаг 2. Тесты для `getAll` ✅ ВЫПОЛНЕНО (расширить `RatingParametersControllerGetAllTest`)

Существовал только `getAllReturnsPageWithSavedParameters`. Добавлены:

| # | Тест | Сценарий / ожидаемый результат | Статус |
|---|------|-------------------------------|--------|
| T1 | `getAllWithEmptyDatabaseReturnsEmptyPage` | БД пуста → 200, `pageItem.totalElements == 0`, `content` пусто. (Сейчас `subList(0, min(...))` на пустом списке — проверяется явно.) | ✅ |
| T2 | `getAllPaginationFirstPage` | 7 записей, `?page=1&size=5` → `totalElements == 7`, `content.size() == 5`, первые 5 кодов в ожидаемом порядке (сортировка по шагу 3.1). | ✅ |
| T3 | `getAllPaginationLastPartialPage` | 7 записей, `?page=2&size=5` → `content.size() == 2` — последние 2 записи в отсортированном порядке; `isLast()` true. | ✅ |
| T4 | `getAllBeyondTotalPagesReturnsEmptyContent` | 7 записей, `?page=3&size=5` (страница за пределами) → 200, `content` пусто, `totalElements == 7`. Не должно падать (в текущем коде — норм, фиксируем). | ✅ |
| T5 | `getAllDefaultPageAndSize` | Без параметров → 200; при 3 записях: `size == 5`, `number == 1` (по умолчанию первая), `totalElements == 3`. | ✅ |
| T6 | `getAllInvalidPageZeroReturns4xx` | `?page=0` → **после рефакторинга** 400 (BAD_REQUEST). До рефакторинга — 500; тест фиксирует новое поведение из шага 1.2. | ✅ |
| T7 | `getAllNegativeOrZeroSizeReturns4xx` | `?size=-1`, `?size=0` → **после рефакторинга** 400 (BAD_REQUEST). До — 500; фиксируется. | ✅ |
| T8 | `getAllNonNumericPageParamFailsGracefully` | `?page=abc` → 4xx/редирект на страницу ошибки (не 500, не падение приложения); проверить статус и что приложение живо (следующий запрос с валидными параметрами — 200). | ✅ |
| T9 | `getAllWithoutAdminRoleReturns403` | Запрос без роли ADMIN (MockMvc без auth / anonymous) → **403 Forbidden** из-за class-level `@PreAuthorize("hasRole('ADMIN')")`. Важно: authority без префикса `ROLE_`, т.е. в `@WithMockUser(authorities = {"USER"})` — plain `"USER"`. | ✅ |
| T10 | `getAllUnauthenticatedRedirectsToLogin` | Без auth вообще → 3xx редирект на `/login` (entry point) либо 403 — зафиксировать фактическое поведение `SecurityConfig` и проверить именно его. | ✅ |
| T11 | `getAllReturnsSortedByCode` | Вставить записи в БД **в перемешанном порядке** кодов (`ZED`, `ALPHA`, `MID`) → на странице код-строки идут строго по возрастанию (сортировка из шага 3.1). Критично: без сортировки тест ловит неопределённый порядок. | ✅ |
| T12 | `getAllPageSliceMatchesDatabaseOrder` | 6 записей, `?page=2&size=3` → ровно записи с индексами 3–5 отсортированного списка; проверка по кодам и значениям (minValue/maxValue/coefficient). | ✅ |

**Саммари:** Файл расширен с 1 до 13 тестов: T1 — пустая БД (200, `totalElements == 0`); T2/T3 — первая и последняя частичная страницы из 7 записей с проверкой сортированного порядка кодов; T4 — запрос за пределы общего числа страниц → 200 + пустой `content`; T5 — значения по умолчанию (`size = 5`, zero-based `getNumber() == 0`); T6/T7/T8 — невалидная пагинация: `page=0`, `size=-1`/`0` → 400, а `page=abc` (нечисло) → 400 через dispatch на `/error`; T9 — пользователь с authority `"USER"` (без префикса `ROLE_`) получает 403; T10 — неавторизованный GET → 3xx редирект на `/login` (CSRF для GET не нужен); T11 — строгий отсортированный порядок (`ALPHA`, `MID`, `ZED`) при вставке в перемешанном порядке; T12 — конкретный слайс страницы совпадает с порядком БД, коды присутствуют в HTML.

### Шаг 3. Тесты на регресс + запуск ✅ ВЫПОЛНЕН
3.1. Запустить все тесты контроллера: `RatingParametersControllerGetAllTest`, `RatingParametersControllerCreateTest` (create-тесты тоже хитят `/all` — например, `createdRatingParametersAppearsOnAllPageAfterRedirect`).
3.2. Убедиться, что create/edit/delete-тесты не сломались от сортировки/пагинации (они проверяют `containsString(code)` — сортировка не должна ломать).
3.3. Зафиксировать изменение статуса с 500 на 400 для невалидных параметров как осознанное (в комментарии теста + в этом плане).

**Саммари:** Оба тестовых класса запущены совместно: `RatingParametersControllerGetAllTest` — 13 тестов, `RatingParametersControllerCreateTest` — 10 тестов; итого **23/23 зелёные, 0 провалов (BUILD SUCCESS)**. Регрессия create-тестов отсутствует: `createdRatingParametersAppearsOnAllPageAfterRedirect` проходит с новой сортировкой (проверяет `containsString`, порядок не важен). Изменение статуса для невалидных параметров зафиксировано: тесты T6/T7/T8 в комментарии отмечают переход 500 → 400 как осознанное изменение контракта.

## 4. Критерии готовности

- [x] `getAll` использует `findAll(Pageable)`, без `subList`.
- [x] Валидация параметров (4xx на мусор).
- [x] Детерминированная сортировка, покрытая тестом T11/T12.
- [x] Все сценарии T1–T12 зелёные.
- [x] Существующие create/edit/delete-тесты не сломаны (проверка 3.1/3.2).
