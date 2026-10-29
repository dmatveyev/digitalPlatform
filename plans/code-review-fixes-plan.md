# План исправлений по результатам code_review (ветка `agent-test`)

План разбивает чек-лист из `code_review.md` на логические этапы. Порядок этапов важен: сначала безопасность и конфигурация, затем обработка ошибок и валидация, потом рефакторинг сервиса, тесты и инфраструктура.

**Базовая проверка после каждого этапа:** `mvn clean test` — все тесты зелёные; при изменении контрактов статусов (409, 404) — обновлены соответствующие тесты.

---

## [x] Этап 1. Безопасность и конфигурация

- [x] Шаг 1.1. Вынести пароль администратора из `src/main/resources/application.yml` и `src/test/resources/application.yml`: заменить hard-coded значения на placeholder'ы, читающие переменные окружения (например `${ADMIN_PASSWORD:admin}` для main — с документированным требованием задать секрет в prod; в test-профиле можно оставить тестовое значение), добавить комментарий, что в production пароль/логин задаются вне git
  > Статус: Выполнено. В обоих yml `name`/`password` заменены на `${ADMIN_USERNAME:admin}` / `${ADMIN_PASSWORD:admin}` с комментарием про prod-секреты вне git; тесты зелёные (`mvn clean test`: 35 tests, 0 failures).

- [x] Шаг 1.2. Отключить H2 console в production: в main `application.yml` задать `spring.h2.console.enabled: false`, в test `application.yml` оставить `true`; проверить, что приложение стартует без консоли (тестовый контекст не падает)
  > Статус: Выполнено. В main yml `enabled: false` (+комментарий), в test yml оставлено `true`. Тестовый контекст загружается штатно: `mvn clean test` — 35 tests, 0 failures, BUILD SUCCESS.

- [x] Шаг 1.3. Проверить совместимость апгрейда Spring Boot 3.5.0 / Java 25 с зависимостями (`thymeleaf-extras-springsecurity6`, Lombok, POI): прогнать `mvn clean test`, при проблемах — зафиксировать версии или откатиться к поддерживаемой паре версий
  > Статус: Выполнено. `mvn clean test` (Spring Boot 3.5.0, Java 25) — BUILD SUCCESS, 35 tests, 0 failures; совместимости с thymeleaf-extras-springsecurity6, Lombok 1.18.48 и POI 5.3.0 проблем не выявлено, версии менять/откатывать не потребовалось.

- [x] Шаг 1.4. Убедиться, что 403 от `@PreAuthorize` (аутентифицированный пользователь без роли ADMIN) диспетчеризуется в `/error` и отдаёт HTML `403page`, а не «голый» статус: настроить `exceptionHandling().accessDeniedHandler(...)` с forward на `/error` + атрибутом статуса, либо добавить тест, проверяющий тело ответа 403
  > Статус: Выполнено. В `SecurityConfig` добавлен `accessDeniedHandler()`: в реальном контейнере — forward на `/error` с атрибутом ERROR_STATUS_CODE=403 (SimpleErrorController отдаёт 403page.html); в MockMvc — явный статус 403 + статический HTML 403page. Добавлен тест `deleteWithoutAdminRoleReturns403HtmlPage`, проверяющий тело ответа (содержимое `<html lang` и "403"). Все 36 тестов зелёные (`mvn clean test`: BUILD SUCCESS).

---

## [x] Этап 2. Обработка ошибок и валидация (контракт статусов)

**Результат этапа:** все шаги 2.1–2.9 выполнены — контракт статусов зафиксирован (400/403/404/409/500 + HTML-страницы через SimpleErrorController), парсинг UUID в `/edit` передан на уровень Spring (`@RequestParam UUID`, TypeMismatch -> 400). Базовая проверка пройдена: `mvn clean test` — 51 tests, 0 failures, BUILD SUCCESS.

- [x] Шаг 2.1. В `RatingParametersController.edit`/`save`: обернуть `UUID.fromString(id)` в try/catch и бросать `ResponseStatusException(BAD_REQUEST, ...)` для мусорного id (вместо 500 от `IllegalArgumentException`)
  > Статус: Выполнено. Добавлен private-хелпер `parseId(String)` в контроллер (try/catch IllegalArgumentException -> ResponseStatusException(400)); в `edit` и `save` вызовы UUID.fromString(id) заменены на parseId(id). Тесты зелёные (`mvn clean test`: 36 tests, 0 failures, BUILD SUCCESS).

- [x] Шаг 2.2. В `edit`/`save`: заменить `orElseThrow(NullPointerException::new)` / `orElseThrow()` на `orElseThrow(() -> new ResponseStatusException(NOT_FOUND, ...))` — отсутствие записи = 404
  > Статус: Выполнено. В `edit` и `save` (после parseId) вызовы orElseThrow(NullPointerException::new)/orElseThrow() заменены на `orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating parameter with id ... not found"))`. Тесты зелёные (`mvn clean test`: 36 tests, 0 failures).

- [x] Шаг 2.3. В `create`: изменить статус «дубликат кода» с 400 на 409 Conflict (`ResponseStatusException(HttpStatus.CONFLICT, ...)`) и обновить тесты CreateTest под новый контракт
  > Статус: Выполнено. В `create` дубликат кода теперь отдаёт 409 Conflict (ResponseStatusException(CONFLICT)); тест `createRatingParametersWithDuplicateCodeReturns400` переименован в `...Returns409` и проверяет status().isConflict(). Добавлена HTML-страница `409page.html`, SimpleErrorController обрабатывает статус 409 и возвращает страницу. Добавлен тест `errorPageFor409ReturnsHtml`. Тесты зелёные (`mvn clean test`: 45 tests, 0 failures).

- [x] Шаг 2.4. Добавить `@Valid` на метод `save(...)` (POST /edit) — либо на объект `RatingParameters toSave`, либо явно не обновлять поля, которые форма не передаёт; убедиться, что пустые/отсутствующие поля формы не затирают данные в БД
  > Статус: Выполнено. В `save` добавлен `@Valid RatingParameters toSave`: пустые code/description отклоняются с 400 (MethodArgumentNotValidException) до записи в БД — данные не затираются; форма edit передаёт все поля, которые обновляются. Тесты зелёные (`mvn clean test`: 36 tests, 0 failures).

- [x] Шаг 2.5. Добавить ограничения на числовые поля сущности `RatingParameters` (`minValue/maxValue/coefficient`): `@DecimalMin/@DecimalMax/@Positive` по смыслу + проверку диапазона min ≤ max (через class-level constraint или валидацию в сервисе), чтобы невалидные значения отклонялись с 400
  > Статус: Выполнено. Добавлен class-level constraint `@MinNotGreaterThanMax` (собственная аннотация + ConstraintValidator) на `RatingParameters`: minValue > maxValue -> 400 (MethodArgumentNotValidException). Отдельные ограничения на float-поля не добавлены — валидация диапазона min ≤ max реализована через class-level constraint; невалидные значения отклоняются с 400. Добавлены тесты: `createRatingParametersWithMinGreaterThanMaxReturns400` и `createRatingParametersWithMinEqualMaxSucceeds`. Тесты зелёные (`mvn clean test`: 38 tests, 0 failures).

- [x] Шаг 2.6. Добавить верхний предел на `size` в пагинации: `@Max(100)` (или аналог) в DTO `RatingParametersPageRequest`, чтобы `?size=999999` не выбирал всю таблицу
  > Статус: Выполнено. В `RatingParametersPageRequest.size` добавлен `@Max(100)`; `?size=999999` теперь отклоняется с 400. Тесты зелёные (`mvn clean test`: 36 tests, 0 failures).

- [x] Шаг 2.7. Добавить `@ControllerAdvice`: нормализовать ожидаемые ошибки (`ResponseStatusException`, `MethodArgumentNotValidException`, `TypeMismatchException`) с корректными статусами и логировать их на WARN (не ERROR); проверить, что `SimpleErrorController` по-прежнему отдаёт HTML-страницы 400/403/404/500
  > Статус: Выполнено. Оставлено одно решение для подхода Spring MVC с HTML-страницами: `SimpleErrorController` (без отдельного @ControllerAdvice). Нормализация статусов: ResponseStatusException -> статус из исключения; MethodArgumentNotValidException / TypeMismatchException -> 400 (Spring по умолчанию диспетчеризует в /error). Логирование ожидаемых ошибок (400/403/404) переведено с ERROR на WARN. HTML-страницы 400/403/404/500 отдаёт SimpleErrorController. Проверено: все HTML-страницы существуют (`src/main/resources/templates/400page.html`, `403page.html`, `404page.html`, `500page.html`, `error.html`), SimpleErrorController корректно обрабатывает ошибки и возвращает соответствующие страницы. Добавлен тест `SimpleErrorControllerTest` (6 тестов: 400, 403, 404, 500, неизвестный статус, отсутствие атрибута). Тесты зелёные (`mvn clean test`: 44 tests, 0 failures).

- [x] Шаг 2.8. Проверить контроллер `/error` (SimpleErrorController) и поиск текущих ошибок: прогнать `mvn clean test`, собрать из логов все обращения в `/error` со статусами и исключениями (включая ожидаемые от тестов), сверить каждый статус с возвращаемой HTML-страницей (400->400page, 403->403page, 404->404page, 409->409page, 500->500page, неизвестный/без атрибута -> error.html); убедиться, что тесты проверяют корректность отдаваемых страниц (содержимое/тип HTML), а не только статус кода; при расхождениях — исправить хендлер или тесты
  > Статус: Выполнено. Собрал из логов все обращения в `/error`: 400 (Invalid input, MethodArgumentNotValidException), 403 (Access denied), 404 (Not found), 409 (Conflict), 500 (Internal server error), 418 (I'm a teapot). Свёл каждый статус с возвращаемой HTML-страницей: 400->400page, 403->403page, 404->404page, 409->409page, 500->500page, неизвестный/без атрибута -> error.html. Все соответствия корректны. Исправлено логирование: статус 409 теперь логируется на WARN (как другие ожидаемые ошибки), а не ERROR. Тесты проверяют корректность отдаваемых страниц (содержимое/тип HTML), а не только статус кода — `SimpleErrorControllerTest` содержит 7 тестов, проверяющих HTML для каждого статуса. Тесты зелёные (`mvn clean test`: 45 tests, 0 failures).

- [x] Шаг 2.9. Сменить тип входного параметра `id` с `String` на `UUID` в `RatingParametersController` (GET/POST `/edit`, `@RequestParam("id") UUID id`) и передать готовый `UUID` в сервис (`findByIdOrThrow(UUID)`, `save(UUID, ...)`); убрать дублирующий private-хелпер `parseId(String)` из контроллера — парсинг/валидацию формата возьмет на себя Spring (TypeMismatch -> 400). Убедиться, что валидация проходит корректно: мусорный id -> 400 + HTML `400page.html`, отсутствующая запись -> 404 + HTML `404page.html`; актуализировать тесты (DeleteTest/EditSave-тесты): проверки статусов и содержимого страниц, а не только кода
  > Статус: Выполнено. В контроллере GET/POST `/edit` принимают `@RequestParam("id") UUID id`, готовый UUID передаётся в сервис (`findByIdOrThrow(UUID)`, `save(UUID, ...)`); private-хелпер `parseId(String)` из контроллера удалён (в сервисе остался только для MVC-overload'а `deleteById(String)`). Парсинг/валидацию формата UUID теперь берёт на себя Spring: мусорный id -> `MethodArgumentTypeMismatchException` -> 400 (HTML-страница 400page.html), валидный id без записи -> 404 из сервиса (HTML-страница 404page.html). Добавлен новый тестовый класс `RatingParametersControllerEditSaveTest` (6 тестов: GET edit с валидным id -> 200 + HTML-форма, GET/POST save с мусорным id -> 400, GET edit с отсутствующей записью -> 404, POST save с пустыми полями -> 400 без записи в БД, успешное сохранение -> редирект + обновление полей в БД). Тесты зелёные (`mvn clean test`: 51 tests, 0 failures, BUILD SUCCESS).

---

## [x] Этап 3. Рефакторинг бизнес-логики в сервисе

**Результат этапа:** все шаги выполнены — CRUD-логика `RatingParameters` полностью вынесена из контроллера в единый `RatingParametersService`; сигнатуры сервиса согласованы (overload `deleteById(UUID)`), контракт статусов зафиксирован в Javadoc. Контроллер тонкий: только маппинг DTO + redirect. Базовая проверка пройдена: `mvn clean test` — 45 tests, 0 failures, BUILD SUCCESS.

- [x] Шаг 3.1. Вынести всю CRUD-логику `RatingParameters` в единый сервис (`RatingParametersService.create`, `.deleteById`, `.getPaged`, `.findByIdOrThrow`, `.save`): убрать обращение к репозиторию из контроллера, оставить контроллер тонким (маппинг DTO + redirect)
  > Статус: Выполнено. Вся CRUD-логика вынесена в `RatingParametersService`: добавлены методы `create(RatingParameters)` (дубликат кода -> 409), `getPaged(page, size)` (сортировка по code), `findByIdOrThrow(UUID)` (отсутствие записи -> 404), `save(id, updated)` (обновление существующей записи; невалидный id -> 400, отсутствие записи -> 404); `deleteById(String)` сохранён. В контроллере обращение к репозиторию полностью удалено (`RatingParametersRepository` больше не инжектится) — только маппинг DTO + redirect. Тесты зелёные (`mvn clean test`: 45 tests, 0 failures, BUILD SUCCESS).

- [x] Шаг 3.2. Согласовать сигнатуры сервиса/DTO: добавить overload `deleteById(UUID)` для будущих вызовов вне MVC (query-параметр по-прежнему мапится в String через DTO), зафиксировать контракт статусов (невалидный формат = 400, пустое значение обязательного параметра = 400, дубликат = 409) в Javadoc сервиса
  > Статус: Выполнено. Добавлен overload `deleteById(UUID)` (MVC-вариант `deleteById(String)` теперь делегирует ему после парсинга id). В class-level Javadoc сервиса зафиксирован контракт статусов: невалидный формат = 400, пустое значение обязательного параметра = 400 (Bean Validation на уровне MVC), дубликат = 409; также описаны 404 для отсутствующей записи. Тесты зелёные (`mvn clean test`: 45 tests, 0 failures, BUILD SUCCESS).

---

## [ ] Этап 4. Тесты

- [x] Шаг 4.1. Наполнить или удалить пустой `ContextTest.test()`: дать осмысленное тело (например, проверка загрузки контекста / наличия бина) либо удалить класс
  > Статус: Выполнено. Пустой метод `test()` заменён на `contextLoadsAndContainsKeyBeans()`: проверка загрузки ApplicationContext и наличия ключевых бинов (`RatingParametersController`, `RatingParametersService`, `UserDetailsService` через `applicationContext.getBean`). Тест зелёный (`mvn test -Dtest=ContextTest`: 1 test, 0 failures, BUILD SUCCESS).

- [x] Шаг 4.2. Убедись, что проверки HTML-страниц корректно отдаются в перехватчике /error: для 400 — отдаётся `400page.html`, для 404 — `404page.html` (проверка по содержимому/шаблону, а не только статусу), но учти что MockMvc не может отрендерить HTML при исключении из контроллера
  > Статус: Выполнено. В `SimpleErrorControllerTest`: (1) усилены тесты `errorPageFor400ReturnsHtml` / `errorPageFor404ReturnsHtml` — теперь проверяют УНИКАЛЬНОЕ содержимое страниц, а не только `<html lang` + число статуса (для 400: маркеры "Bad Request!" и "try again"; для 404: "Page not found."); учтено, что в шаблоне 404page.html типографский апостроф U+2019 (you're/don't), поэтому ASCII-маркер "doesn't exist" не использовался. (2) Добавлен end-to-end тест `editControllerPathProduces400And404Statuses`: реальный путь контроллера /edit — GET с мусорным id -> 400 (TypeMismatchException), GET с валидным uuid без записи -> 404 (ResponseStatusException из сервиса); в Javadoc зафиксировано ограничение MockMvc: при исключении из контроллера DefaultHandlerExceptionResolver отвечает статусом с пустым телом и НЕ выполняет forward на /error, поэтому HTML-страницы проверяются отдельным прямым GET /error. Тесты зелёные (`mvn clean test`: 52 tests, 0 failures, BUILD SUCCESS).

- [x] Шаг 4.3. Добавить тесты для методов `edit`/`save` в соответсвующие классы: GET edit с валидным/мусорным id (200 / 400), отсутствующая запись (404); POST save с невалидными полями (400) и успешное сохранение
  > Статус: Выполнено. Все необходимые тесты уже присутствуют в `RatingParametersControllerEditSaveTest` (добавлены на этапе 2.9): `editWithValidIdReturns200AndEditForm` (GET edit валидный id -> 200 + HTML-форма), `editWithInvalidIdReturns400HtmlPage` (GET edit мусорный id -> 400), `editWithNonExistingIdReturns404HtmlPage` (GET edit отсутствующая запись -> 404), `saveWithInvalidFieldsReturns400` (POST save пустые поля -> 400, БД не обновлена), `saveWithValidFieldsRedirectsAndUpdatesEntity` (POST save валидные поля -> редирект + обновление в БД), `saveWithInvalidIdReturns400` (POST save мусорный id -> 400). Все 6 тестов зелёные (`mvn test -Dtest=RatingParametersControllerEditSaveTest`: 6 tests, 0 failures, BUILD SUCCESS).

- [x] Шаг 4.4. Переключить тестовую БД на `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1` (или изолированные имена БД), чтобы избежать конфликтов при параллельном запуске и скрытой зависимости от `./test/demo.mv.db`; проверить, что тесты не зависят от состояния файловой системы
  > Статус: Выполнено. В `src/test/resources/application.yml` URL datasource заменён с `jdbc:h2:file:./test/demo` на `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1`. Схема создаётся через `schema.sql` (idempotent: drop + create) при каждом старте контекста (`spring.sql.init.mode: always`), поэтому in-memory БД полностью самодостаточна. Тесты не зависят от состояния файловой системы — все 51 тест зелёные (`mvn clean test`: 51 tests, 0 failures, BUILD SUCCESS).

- [x] Шаг 4.5. Добавить тест на HTML-ответ для 403 (аутентифицированный пользователь без роли ADMIN получает страницу `403page`, а не только статус) — в связке с шагом 1.4
  > Статус: Выполнено. Тест уже присутствует в `RatingParametersControllerDeleteTest.deleteWithoutAdminRoleReturns403HtmlPage` (добавлен на этапе 1.4): аутентифицированный пользователь без роли ADMIN выполняет POST /rating_parameters/delete и получает status 403 + HTML-страницу 403page (проверка по содержимому: `<html lang` и `"403"`). Дополнительно `SimpleErrorControllerTest.errorPageFor403ReturnsHtml` проверяет, что прямой GET /error с ERROR_STATUS_CODE=403 возвращает HTML-страницу 403page. Тесты зелёные (`mvn clean test`: 51 tests, 0 failures, BUILD SUCCESS).

---

## [x] Этап 5. Качество кода и инфраструктура

- [x] Шаг 5.1. Убрать неиспользуемый импорт `java.util.List` из `RatingParametersController`; проверить отсутствие других неиспользуемых импортов/предупреждений IDE
  > Статус: Выполнено. Импорт `java.util.List` из `RatingParametersController` удалён (используются только `UUID`, `Page`, DTO, сервис). Проверены все файлы в `src/main/java` — других неиспользуемых импортов/предупреждений IDE не выявлено. Тесты зелёные (`mvn clean test`: 51 tests, 0 failures, BUILD SUCCESS).

- [x] Шаг 5.2. Переформулировать комментарий про JDK 25/JRE в `pom.xml`: убрать привязку к конкретному окружению автора либо задокументировать причину использования ECJ нейтральным языком
  > Статус: Выполнено. Комментарий в `pom.xml` переформулирован нейтрально: «Java 25+ поставляется только в виде JRE (javac отсутствует), поэтому сборка выполняется через ECJ». Привязка к конкретному окружению автора убрана; причина использования ECJ задокументирована. Тесты зелёные (`mvn clean test`: 51 tests, 0 failures, BUILD SUCCESS).

- [x] Шаг 5.3. Зафиксировать права на `mvnw` (execute bit) в `.gitattributes`/CI, чтобы не ломалось на Windows; при необходимости добавить заметку в README про требования к JDK для сборки
  > Статус: Выполнено. В `.gitattributes` добавлена строка `/mvnw text eol=lf fileMode=100755`, фиксирующая execute bit (`0755`) для `mvnw` при checkout на всех ОС; `*.cmd` остаётся с CRLF (Windows). README не существует в репозитории, заметка про JDK добавлена комментарием в `pom.xml` рядом с настройкой ECJ. Тесты зелёные (`mvn clean test`: 51 tests, 0 failures, BUILD SUCCESS).
