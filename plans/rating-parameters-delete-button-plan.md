# План: кнопка удаления на rating_parameters.html + Bean Validation для delete

## Контекст и текущее состояние

- `RatingParametersController.delete` (`src/main/java/com/example/digitalplatform/controller/RatingParametersController.java:80-86`) — рефакторинг уже выполнен (см. `plans/rating-parameters-delete-refactor-plan.md`, все шаги `[x]`): метод тонкий, делегирует в `RatingParametersService.deleteById(String)`.
- `RatingParametersService.deleteById` (`src/main/java/com/example/digitalplatform/service/RatingParametersService.java:27-39`) — парсинг UUID вручную (try/catch → 400), проверка существования (→ 404), удаление. Поведение 400/404 остаётся без изменений.
- Форма `rating_parameters.html` (`src/main/resources/templates/rating_parameters/rating_parameters.html:48-51`) содержит только кнопку «Ред...»; кнопки/формы удаления нет.
- Bean Validation в проекте применяется через DTO-обёртку query-параметров с `@Valid @ModelAttribute`: эталон — `RatingParametersPageRequest` (`src/main/java/com/example/digitalplatform/controller/dto/RatingParametersPageRequest.java`) для `getAll`. Ошибки → 400 → `/error` → `SimpleErrorController` → `400page`.
- CSRF включён глобально (`SecurityConfig.java:75`, исключение только `/h2-console/**`). POST из формы обязателен с CSRF-токеном (Spring добавляет автоматически в Thymeleaf-формы).
- Тесты delete существуют и зелёные: `src/test/java/com/example/digitalplatform/controller/RatingParametersControllerDeleteTest.java` (5 тестов; мусорный id `"abc"` уже ожидает 400, все POST-тесты используют `.with(csrf())`).

## Ключевые решения (зафиксированы)

1. **DTO-обёртка для delete**: новый класс `RatingParametersDeleteRequest` в пакете `com.example.digitalplatform.controller.dto`, поле `String id` с `@NotBlank`. Контроллер: `delete(@Valid @ModelAttribute RatingParametersDeleteRequest request)` → делегирование в сервис. Ровно повторяет конвенцию `getAll`/`RatingParametersPageRequest`; без `@Valid` Bean Validation не срабатывает для model-атрибутов.
2. **Парсинг UUID остаётся в сервисе** (400 через try/catch). Дублирующая аннотация `@Pattern(UUID)` на DTO не добавляется: парсинг уже обрабатывает мусор, а `@NotBlank` закрывает пустые/отсутствующие параметры. Поведение 400/404 не меняется.
3. **Форма**: в ячейке действий строки таблицы (рядом с «Ред...») — `<form th:action="@{/rating_parameters/delete}" th:method="post">` со скрытым полем `id`; подтверждение через JS `confirm()`. Реализация повторяет эталон проекта — форму «Завершить» в `src/main/resources/templates/requests/editRequest.html:110-115` (формат `<form th:action th:method="post">` + `<input class="btn btn-danger" type="submit" value="Удалить" onClick="return confirm('...')">`).
4. **Тесты**: расширяют существующий `RatingParametersControllerDeleteTest` (добавление тестов, а не новый класс): 400 для пустого id и отсутствие параметра — новое поведение Bean Validation; остальные сценарии (успех 3xx, 404, 403, /login, мусорный `"abc"` → 400) уже покрыты.

## Этап 1. Рефакторинг метода delete (Bean Validation входных параметров)

- [x] Шаг 1.1. Создать DTO `src/main/java/com/example/digitalplatform/controller/dto/RatingParametersDeleteRequest.java`: Lombok `@Data`, поле `String id` с аннотацией `jakarta.validation.constraints.NotBlank`; Javadoc в стиле `RatingParametersPageRequest`.
> Статус: выполнено.

- [x] Шаг 1.2. Изменить подпись метода `delete` в `src/main/java/com/example/digitalplatform/controller/RatingParametersController.java:80-86`: заменить `@RequestParam("id") String id` на `@Valid @ModelAttribute RatingParametersDeleteRequest request`, передавать `request.getId()` в `ratingParametersService.deleteById(...)`; вернуть `"redirect:/rating_parameters/all"`. Добавить импорт DTO.
> Статус: выполнено.

- [x] Шаг 1.3. Проверить компиляцию: `mvn compile -q` — без ошибок (импорты, сигнатура метода).
> Статус: выполнено.

- [x] Шаг 1.4. Запустить существующие тесты delete до расширения: `mvn test -Dtest=RatingParametersControllerDeleteTest`. Ожидаемо зелёные (5/5) — рефакторинг сигнатуры не меняет наблюдаемое поведение для всех текущих сценариев (id передаётся query-параметром, мапится в поле DTO).
> Статус: выполнено.

## Этап 2. Реализация удаления с формы rating_parameters.html

- [x] Шаг 2.1. В `src/main/resources/templates/rating_parameters/rating_parameters.html` (ячейка действий строки, рядом с «Ред...», строки 48-51) добавить форму по образцу `requests/editRequest.html:110-115`: `<form th:action="@{/rating_parameters/delete}" th:method="post">`, скрытое поле `<input type="hidden" name="id" th:value="${r_param.id}"/>` и `<input class="btn btn-outline-danger" type="submit" value="Удалить" onClick="return confirm('Удалить параметр?')"/>`.
> Статус: выполнено.

- [x] Шаг 2.2. Добавить тесты в `src/test/java/com/example/digitalplatform/controller/RatingParametersControllerDeleteTest.java` по образцу существующих (с `.with(csrf())`, `@WithMockUser(authorities = {"ADMIN"})`):
  - POST `/rating_parameters/delete` с пустым id (`id=`) → **400 Bad Request** (Bean Validation, `@NotBlank`) + запись не удалена;
  - POST `/rating_parameters/delete` без параметра id → **400 Bad Request**.
> Статус: выполнено.

- [x] Шаг 2.3. Запустить тесты delete: `mvn test -Dtest=RatingParametersControllerDeleteTest`. Все тесты зелёные, в т.ч. новые на 400 для пустого/отсутствующего id.
> Статус: выполнено.

- [x] Шаг 2.4. Добавить тесты в существующий класс `src/test/java/com/example/digitalplatform/controller/RatingParametersControllerDeleteTest.java`, проверяющие, что на форме `rating_parameters/rating_parameters` присутствует кнопка удаления: GET `/rating_parameters/all` (с `@WithMockUser(authorities = {"ADMIN"})`) → 200 OK, в HTML-ответе (`mvcResult.getResponse().getContentAsString()`) присутствуют форма удаления и текст кнопки «Удалить» (например, проверка по подстроке `value="Удалить"` / `th:action="@{/rating_parameters/delete}"` — фиксировать конкретные маркеры при реализации).
> Статус: выполнено. Добавлен тест `ratingParametersPageContainsDeleteButton`: GET `/rating_parameters/all` → 200 OK; в финальном HTML проверяются маркеры `action="/rating_parameters/delete"` (Thymeleaf подставляет `th:action`) и `value="Удалить"`. Тесты delete зелёные (8/8).

- [x] Шаг 2.5. Полный прогон всех тестов для регрессии: `mvn test`. Ожидаемо зелёные все классы, в т.ч. обновлённый `RatingParametersControllerDeleteTest` (7 тестов); ожидаемый ERROR-лог из `SimpleErrorController` на 400/404 — норма.
> Статус: выполнено.

## Риски и ограничения

- **Изменение сигнатуры метода** (`@RequestParam` → DTO): query-параметр `id` сохраняется, поэтому существующие запросы и тесты не ломаются; риск минимальный.
- **CSRF**: форма POST обязана содержать CSRF-токен — Spring Security добавляет его автоматически для Thymeleaf-форм; проверять в Шаге 2.4 (тесты используют `.with(csrf())`). Без токена браузер получит 403 — это индикатор ошибки разметки формы, а не кода.
- **Не трогать** `SimpleErrorController`, шаблоны ошибок, `RatingParametersService` и существующие тесты `create`/`getAll` — только расширяется `DeleteTest`.
- **Подтверждение удаления** через JS `confirm()` — UX-решение; при отключённом JS форма отправится без подтверждения (допустимо).
