# План: рефакторинг `delete` в RatingParametersController + тесты (с выносом в сервис)

## Контекст

Метод `RatingParametersController.delete` (`src/main/java/com/example/digitalplatform/controller/RatingParametersController.java:78-84`):

```java
@PostMapping("/delete")
public String delete(@RequestParam("id") String id, Model model) {
    ratingParametersRepository.deleteById(UUID.fromString(id));
    List<RatingParameters> list = ratingParametersRepository.findAll();   // (1) лишний запрос ко всей БД
    model.addAttribute("rating_parameters", list);                          // (2) атрибут никто не читает — сразу redirect
    return "redirect:/rating_parameters/all";                               // (3) redirect сбрасывает model
}
```

Проблемы (по убыванию критичности):

1. **Нет обработки отсутствующей записи.** `deleteById(UUID)` при отсутствии записи с таким UUID молча ничего не делает, и контроллер всё равно отдаёт 3xx редирект на `/all`, как будто удаление прошло успешно. Пользователь/клиент не может отличить «удалили» от «ничего не было». Это главное отличие от `create` (там есть явная проверка уникальности) — в `delete` нет ни `findById().orElseThrow(...)`, ни проверки существования.
2. **Нет обработки невалидного UUID.** `UUID.fromString(id)` при мусорном значении (`"abc"`) выбрасывает `IllegalArgumentException`. Это необработанное runtime-исключение → 500 Internal Server Error (а не 400). Для сравнения: валидные ошибки ввода в проекте дают 400 через `/error` → `SimpleErrorController`.
3. **Лишний запрос к БД + бессмысленный model-атрибут.** `findAll()` после удаления делает полный SELECT по всей таблице, а затем кладёт список в `model`, но метод сразу возвращает `redirect:` — при редиректе модель текущего request сбрасывается, и этот атрибут никогда не используется. Оба строки — мёртвый код / лишняя нагрузка на БД.
4. **Бизнес-логика удаления лежит в контроллере.** Метод сам парсит UUID, проверяет (нет) существование и вызывает репозиторий. Это затрудняет переиспользование логики и её юнит-тестирование без полного Spring-контекста — при выносе в сервисный слой контроллер становится тонким, а логика тестируема изолированно.
5. **Нет тестов.** Для `create` есть эталонный класс `RatingParametersControllerCreateTest`, для `getAll` — `RatingParametersControllerGetAllTest`. Для `delete` теста нет вообще, поэтому регрессия в поведении удаления не покрывается.

## Оценка необходимости рефакторинга

**Рефакторинг обоснован (средний приоритет).** Метод короткий, но содержит реальную логическую дыру (п.1 — тихий успех при отсутствии записи), необработанное исключение (п.2), мёртвый код (п.3) и бизнес-логику в контроллере (п.4). Это не «косметика»: отсутствие проверки существования меняет наблюдаемое поведение API и расходится с паттерном соседних методов (`edit`/`save` делают `findById(...).orElseThrow(...)`, `create` проверяет дубликат). Плюс полное отсутствие тестов делает метод непроверяемым, а вынос логики в сервис даёт её изолированную тестируемость.

Вместе с тем важно: **это рефакторинг + изменение поведения (behavior change)**, а не чисто механическая перестановка кода. Добавление 404 для отсутствующей записи — новый контракт, который надо зафиксировать тестами; вынос в сервис — структурное изменение, влияющее на зависимости контроллера.

## Решение: 

Совмещаем целевой рефакторинг метода с выносом бизнес-логики удаления в отдельный сервисный слой:

**1. Новый сервис `RatingParametersService`** (`src/main/java/com/example/digitalplatform/service/RatingParametersService.java`, аннотация `@Service`), зависящий от `RatingParametersRepository`. Метод `deleteById(String id)` выполняет:
- парсинг UUID: `UUID.fromString(id)`; при `IllegalArgumentException` → бросает `ResponseStatusException(HttpStatus.BAD_REQUEST, ...)` (единообразие с `create`/`getAll`: плохой ввод = 400);
- проверку существования: `findById(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating parameter with id '" + id + "' not found"))`;
- удаление: `deleteById(uuid)`.

**2. Контроллер становится тонким.** Метод `delete` в контроллере принимает только `@RequestParam("id") String id`, делегирует вызов сервису и возвращает `"redirect:/rating_parameters/all"`. Убираются параметр `Model model`, строки с `findAll()` и `model.addAttribute(...)` (мёртвый код). Контроллер не содержит бизнес-логики.

**3. Поведение:**
- успешное удаление → 3xx редирект на `/rating_parameters/all` + запись удалена из БД;
- отсутствующая валидная запись → **404 Not Found** (через `/error`, `SimpleErrorController` мапит 404 → `"404page"`);
- мусорный/невалидный UUID (`"abc"`) → **400 Bad Request** (через `/error`, `SimpleErrorController` мапит 400 → `"400page"`).

> Примечание: вариант B (явный try/catch на UUID) поглощён в сервис — парсинг и обработка `IllegalArgumentException` живут там же, поэтому отдельного варианта не остаётся. Вариант C (сервисный слой) теперь является частью основного решения, а не опцией.

## Шаги

- [x] Шаг 1. Зафиксировать текущее поведение `delete` как спецификацию: создать каркас тестового класса `src/test/java/com/example/digitalplatform/controller/RatingParametersControllerDeleteTest.java` по образцу `RatingParametersControllerCreateTest` (аннотации `@SpringBootTest(classes = DigitalPlatformApplication.class)`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`; поля `MockMvc` и `RatingParametersRepository`; `@BeforeEach setUp() { ratingParametersRepository.deleteAll(); }`).
> Статус: выполнено. Создан каркас класса с нужными аннотациями (`@SpringBootTest(classes = DigitalPlatformApplication.class)`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`), полями `mockMvc` и `ratingParametersRepository` и `@BeforeEach setUp() { ratingParametersRepository.deleteAll(); }`. Тестовые методы добавляются в Шаге 2.

- [x] Шаг 2. Написать тесты на текущее (до рефакторинга) поведение `delete`, чтобы зафиксировать контракт до изменений. Минимальный набор по образцу эталона:
  - успешное удаление существующей записи → 3xx редирект на `/rating_parameters/all` + запись исчезла из БД;
  - удаление несуществующего (валидного) UUID → текущее поведение (3xx, ничего не ломается);
  - POST без роли ADMIN (`@WithMockUser(authorities = {"USER"})`) → 403 Forbidden;
  - неавторизованный POST (без `@WithMockUser`, с `.with(csrf())`) → 3xx редирект на `/login`;
   - мусорный id (`"abc"`) → текущее поведение (500).
> Статус: выполнено. Добавлены 5 тестов на ТЕКУЩЕЕ (до рефакторинга) поведение delete: успешное удаление (3xx + запись исчезла), несуществующий валидный UUID (текущий «тихий успех» 3xx), 403 без роли ADMIN, редирект на /login для неавторизованного POST, мусорный id "abc" (500). Добавлен импорт java.util.UUID.

- [x] Шаг 3. Скомпилировать проект до рефакторинга: `mvn compile -q` (сборка идёт через ECJ согласно `pom.xml`). Убедиться, что базовое состояние зелёное. Если есть ошибка компиляции, поправить ее, используя не более 1 попытки
> Статус: выполнено. `mvn compile -q` завершился успешно (exit 0), ошибок компиляции нет — только JVM-предупреждения об Unsafe. Базовое состояние зелёное.

- [x] Шаг 4. Запустить созданный тестовый класс ДО изменения кода: `mvn test -Dtest=RatingParametersControllerDeleteTest`. Все тесты должны пройти и зафиксировать текущее поведение (в т.ч. «тихий успех» при отсутствии записи и 500 на мусорном UUID).
> Статус: выполнено. `mvn test -Dtest=RatingParametersControllerDeleteTest` — Tests run: 5, Failures: 0, Errors: 0, BUILD SUCCESS. Текущее поведение зафиксировано (успешное удаление 3xx + запись исчезла; «тихий успех» 3xx при отсутствии валидного UUID; 403 без ADMIN; редирект на /login для неавторизованного; мусорный "abc" → необработанное IllegalArgumentException из UUID.fromString).
> Примечание: первый запуск дал 1 error — тест на "abc" ожидал HTTP 500, но MockMvc (MockServlet) не диспетчеризует исключение в /error и пробрасывает его наружу. Исправление (единственная допущенная попытка): тест теперь проверяет, что из mockMvc.perform(...) вылетает ServletException с корневой причиной IllegalArgumentException("...abc") — это и есть текущее «500»-поведение на уровне MockMvc.

- [x] Шаг 5. Создать сервис `RatingParametersService` (`src/main/java/com/example/digitalplatform/service/RatingParametersService.java`, `@Service`) с методом `deleteById(String id)`: парсинг UUID (с перехватом `IllegalArgumentException` → `ResponseStatusException(HttpStatus.BAD_REQUEST, ...)`), проверка существования через `findById(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating parameter with id '" + id + "' not found"))`, затем `ratingParametersRepository.deleteById(uuid)`.
> Статус: выполнено. Создан сервис `@Service @RequiredArgsConstructor` с `private final RatingParametersRepository` и методом `deleteById(String id)`: try/catch на UUID.fromString → 400 BAD_REQUEST; findById(...).orElseThrow(...) → 404 NOT_FOUND ("Rating parameter with id '<id>' not found"); затем deleteById(uuid). Соответствует конвенциям проекта (см. RatingService).

- [x] Шаг 6. Рефакторинг контроллера `delete` в `src/main/java/com/example/digitalplatform/controller/RatingParametersController.java`: удалить параметр `Model model` и строки с `findAll()`/`model.addAttribute(...)`; метод принимает только `@RequestParam("id") String id`, вызывает `ratingParametersService.deleteById(id)` и возвращает `"redirect:/rating_parameters/all"`. Добавить зависимость от `RatingParametersService` (через конструктор, Lombok `@RequiredArgsConstructor`). Убедиться, что импорты корректны (`HttpStatus`/`ResponseStatusException` остаются — использует их `create`; `Model` при удалении параметра не оставляет неиспользуемых импортов).
> Статус: выполнено. Метод delete теперь принимает только @RequestParam("id") String id и делегирует в ratingParametersService.deleteById(id); убраны Model, findAll(), model.addAttribute(...). Добавлен `private final RatingParametersService` (через @RequiredArgsConstructor) + импорт com.example.digitalplatform.service.RatingParametersService. Все импорты остались используемыми: HttpStatus/ResponseStatusException — create; Model/List/RatingParameters/UUID — остальные методы (getAll/edit/save/showCreateForm).

- [x] Шаг 7. Скомпилировать после рефакторинга: `mvn compile -q`. Убедиться в отсутствии ошибок (особенно импортов и новых зависимостей сервисного слоя).
> Статус: выполнено. `mvn compile -q` — exit 0, без ошибок (в т.ч. по импортам и новой зависимости RatingParametersService). Сборка зелёная.

- [x] Шаг 8. Обновить тесты под новое поведение (по образцу эталона):
  - успешное удаление существующей записи → 3xx редирект на `/rating_parameters/all` + запись удалена из БД (`findById(...)` пуст / `count()` уменьшился);
  - удаление несуществующего валидного UUID → **404 Not Found** (через `/error`, `SimpleErrorController` мапит 404 → `"404page"`);
   - невалидный UUID (`"abc"`) → **400 Bad Request** (через `/error`, `SimpleErrorController` мапит 400 → `"400page"`);
   - 403 без роли ADMIN и редирект на `/login` для неавторизованного (поведение не меняется).
> Статус: выполнено. Тесты обновлены под новое поведение: успешное удаление (3xx + findById пуст), несуществующий валидный UUID → 404 Not Found, невалидный "abc" → 400 Bad Request (с проверкой, что запись НЕ удалена), 403 без ADMIN и редирект на /login для неавторизованного — поведение не менялось. Обновлён class-level doc-комментарий под новый контракт.

- [x] Шаг 9. Запустить тесты после рефакторинга: `mvn test -Dtest=RatingParametersControllerDeleteTest`. Все тесты должны пройти, в т.ч. новые тесты на 404 для отсутствующей записи и 400 для невалидного UUID.
> Статус: выполнено. `mvn test -Dtest=RatingParametersControllerDeleteTest` — Tests run: 5, Failures: 0, Errors: 0, BUILD SUCCESS. Прошли все тесты, включая новые: несуществующий валидный UUID → 404 и невалидный "abc" → 400 (оба через /error/SimpleErrorController).

- [x] Шаг 10. Полный прогон всех тестов для регрессии: `mvn test` (вывод отфильтровать по `Tests run|Running|ERROR|BUILD|FAIL`). Убедиться, что `RatingParametersControllerCreateTest`, `RatingParametersControllerGetAllTest` и новый `RatingParametersControllerDeleteTest` зелёные (ожидаемый ERROR-лог от `SimpleErrorController` на 400/404 в тестах — норма).
> Статус: выполнено. `mvn test` — Tests run: 32, Failures: 0, Errors: 0, BUILD SUCCESS. Зелёные: RatingParametersControllerCreateTest (10), GetAllTest (13), новый DeleteTest (5) + остальные классы. Единственный ERROR-лог в выводе — ожидаемый log.error из SimpleErrorController на 400 в create-тесте (норма). Регрессии нет.

## Риски и ограничения

- **Изменение поведения:** добавление 404 для отсутствующей записи и 400 для невалидного UUID меняют наблюдаемый контракт (ранее — тихий 3xx / 500). Это осознанное решение; зафиксировано тестами. Если такое поведение нежелательно, вернуть исходный вариант без `orElseThrow`/обработки UUID — но тогда дефекты п.1 и п.2 остаются.
- **Новый сервисный слой:** появляется класс `RatingParametersService`. Это расширяет архитектуру (контроллер → сервис → репозиторий), но не затрагивает другие методы контроллера — только `delete` делегирует в сервис. Если нужен единообразный вынос всех методов, это отдельная задача.
- **Не трогать** `SimpleErrorController` и шаблоны ошибок — ветки `"404page"`/`"500page"`/`"400page"` уже существуют, менять не нужно.
- **Тесты на `getAll`/`create` не изменять** — они спецификация; проверяем только отсутствие регрессии полным прогоном (Шаг 10).
- **Контекст ограничен (32к):** шаги выполняются по одному, после каждого сразу обновляется этот файл: `[x]` + краткое саммари в блоке `> Статус:`. При падении с ошибкой — ошибка записывается туда же и работа останавливается.
