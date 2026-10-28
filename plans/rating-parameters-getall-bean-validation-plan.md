# План: Bean Validation для `getAll` в RatingParametersController

## Контекст

Метод `RatingParametersController.getAll` (`src/main/java/com/example/digitalplatform/controller/RatingParametersController.java:27-40`) принимает query-параметры `page`/`size` как `int @RequestParam(defaultValue=...)` и проверяет их вручную:

```java
if (page < 1 || size <= 0) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ...); }
```

Спецификация уже написана в `src/test/java/com/example/digitalplatform/controller/RatingParametersControllerGetAllTest.java`:
- T6: `?page=0&size=5` → **400**
- T7: `?size=-1`, `?size=0` → **400**
- T8: `?page=abc` → **400** (TypeMismatchException до вызова метода)
- T4: `?page=10&size=5` при 3 записях → **200**, content пуст (страница за пределами данных — валидная)

Цель: заменить ручную проверку на Bean Validation по аналогии с `create` (`@Valid` + аннотации), чтобы ошибка шла через стандартный путь: исключение → `/error` → `SimpleErrorController.handleError` (уже мапит 400 → `"400page"`) → существующий шаблон `src/main/resources/templates/400page.html`.

## Решение (утверждено)

**DTO-обёртка для query-параметров.** Новый класс `RatingParametersPageRequest` в package `com.example.digitalplatform.controller.dto` (конвенция проекта — см. `CreateRequestDto.java:1-32`): Lombok `@Data`, поля `int page = 1; int size = 5;` с аннотацией `@Min(1)` на каждом. Метод принимает параметр как `@Valid @ModelAttribute`. Spring сам мапит query-параметры в поля DTO, а нарушение ограничений даёт `MethodArgumentNotValidException` (400).

## Проверенные факты (не требуют действий)

- `spring-boot-starter-validation` уже есть в `pom.xml:51-53` — добавлять не нужно.
- Шаблон `400page.html` существует и рендерит страницу 400 — менять не нужно.
- `SimpleErrorController.handleError` (`src/main/java/com/example/digitalplatform/controller/SimpleErrorController.java:29-31`) уже возвращает `"400page"` для статуса 400 — менять не нужно.
- В проекте нет `@ControllerAdvice`/кастомных exception resolvers — исключения диспетчеризуются в `/error` штатно.
- T8 (`?page=abc`) покрывается сам Spring: преобразование строки в int падает с `TypeMismatchException` до входа в метод → тоже 400 через `/error`. Правки для этого не нужны.

## Шаги

- [x] Шаг 1. Создать DTO `RatingParametersPageRequest` (package `com.example.digitalplatform.controller.dto`): Lombok `@Data`, поля `int page = 1;` и `int size = 5;`, каждая с `jakarta.validation.constraints.Min(1)`.
> **Статус:** Выполнен. Создан файл `src/main/java/com/example/digitalplatform/controller/dto/RatingParametersPageRequest.java`: package `com.example.digitalplatform.controller.dto`, Lombok `@Data`, поля `int page = 1;` и `int size = 5;`, оба с `jakarta.validation.constraints.Min(1)`. Проверка компиляцией: `mvn compile -q` завершилась успешно (без ошибок).

- [x] Шаг 2. Изменить сигнатуру `getAll` в `RatingParametersController`: убрать два `@RequestParam`, принять `@Valid @ModelAttribute RatingParametersPageRequest request`; удалить ручной `if (page < 1 || size <= 0)` и `throw new ResponseStatusException(...)`; использовать `request.getPage() - 1` и `request.getSize()` в `PageRequest.of(..., Sort.by("code"))`. Убрать ставшие неактуальные импорты (`HttpStatus`, `ResponseStatusException`).
> **Статус:** Выполнен. В `RatingParametersController.getAll` сигнатура заменена на `@Valid @ModelAttribute RatingParametersPageRequest request`; ручная проверка и бросок `ResponseStatusException` удалены; пагинация строится через `request.getPage() - 1` / `request.getSize()`; импорты `HttpStatus` и `ResponseStatusException` убраны, добавлен импорт DTO.

- [x] Шаг 3. Скомпилировать проект: `mvn compile -q` (сборка идёт через ECJ согласно `pom.xml:135-147`).
> **Статус:** Выполнен с одной поправкой, внесённой в ходе шага. Первая попытка компиляции упала: при удалении импортов `HttpStatus`/`ResponseStatusException` в шаге 2 были удалены и импорты, которые всё ещё использует метод `create` (строка 63: `throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ...)`), из-за чего компиляция завершилась с ошибкой `cannot find symbol`. Добавлены обратно импорты `org.springframework.http.HttpStatus` и `org.springframework.web.server.ResponseStatusException`; повторный запуск `mvn compile -q` завершился успешно (без ошибок компиляции).

- [x] Шаг 4. Запустить тесты: `mvn test -Dtest=RatingParametersControllerGetAllTest`. Все 12 тестов должны пройти без изменений (T6/T7 → 400 через валидацию DTO, T8 → 400 через TypeMismatchException, T4 остаётся 200).
> **Статус:** Выполнен. `mvn test -Dtest=RatingParametersControllerGetAllTest` завершился успешно: `Tests run: 13, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`. В логах видно ожидаемое поведение: T6 (`?page=0`) и T7 (`size=-1`/`0`) → `MethodArgumentNotValidException` (нарушение `@Min(1)` в DTO) → 400; T8 (`?page=abc`) → ошибка конвертации строки в int при маппинге DTO → 400.

- [x] Шаг 5. Запустить mvn test (с фильтрацией вывода). Убедиться, что все тесты проходят (BUILD SUCCESS).
> **Статус:** Выполнен. `mvn test` (полный прогон всех тестовых классов, вывод отфильтрован по паттерну `Tests run|Running|ERROR|BUILD|FAIL`) завершился успешно: всего `Tests run: 27, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`. Разбивка: DecisionGeneratorTest — 2 теста, BellmanTest — 1, ContextTest — 1, RatingParametersControllerCreateTest — 10 (ожидаемый ERROR-лог от SimpleErrorController на 400 при тесте валидации create), RatingParametersControllerGetAllTest — 13. Ошибок и провалов нет.

## Риски и ограничения

- Поведение по умолчанию сохраняется: поля DTO инициализированы `page=1`, `size=5`, поэтому запросы без параметров работают как раньше (тест T5).
- Не менять тесты — они спецификация и должны пройти без правок.
- Не трогать `SimpleErrorController` и шаблоны ошибок.
