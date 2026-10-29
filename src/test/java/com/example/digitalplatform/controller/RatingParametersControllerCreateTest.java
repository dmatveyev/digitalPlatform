package com.example.digitalplatform.controller;

import com.example.digitalplatform.db.repository.RatingParametersRepository;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.digitalplatform.db.model.RatingParameters;

import java.util.Collections;

import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.example.digitalplatform.DigitalPlatformApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RatingParametersControllerCreateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingParametersRepository ratingParametersRepository;



    @BeforeEach
    void setUp() {
        ratingParametersRepository.deleteAll();
    }

    /**
     * Тест create в RatingParametersController:
     * POST /rating_parameters/create возвращает 3xx редирект на /rating_parameters/all.
     *
     * Примечание: приложение использует GrantedAuthorityDefaults("") (пустой префикс ролей),
     * поэтому @PreAuthorize("hasRole('ADMIN')") проверяет право 'ADMIN' без префикса ROLE_.
     * Поэтому для прохождения метод-безопасности используется authorities = {"ADMIN"}
     * (эквивалентно роли ADMIN при данном конфигурировании).
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createRatingParametersRedirectsToAll() throws Exception {
        mockMvc.perform(post("/rating_parameters/create")
                        .param("code", "NEW_PARAM")
                        .param("description", "Тестовый параметр")
                        .param("minValue", "1.0")
                        .param("maxValue", "10.0")
                        .param("coefficient", "0.5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating_parameters/all"));

        // Проверяем, что запись реально сохранилась в БД
        RatingParameters entity = ratingParametersRepository.findByCode("NEW_PARAM");
        Assertions.assertNotNull(entity, "Запись с кодом NEW_PARAM не найдена в БД после create");
        Assertions.assertNotNull(entity.getId(), "У сохранённой записи должен быть не-null id (сгенерирован при persist)");
        Assertions.assertEquals("Тестовый параметр", entity.getDescription());
        Assertions.assertEquals(1.0f, entity.getMinValue(), 0.0001f);
        Assertions.assertEquals(10.0f, entity.getMaxValue(), 0.0001f);
        Assertions.assertEquals(0.5f, entity.getCoefficient(), 0.0001f);
    }

    /**
     * Тест create в RatingParametersController при отсутствии прав доступа:
     * пользователь аутентифицирован, но не имеет роли ADMIN -> метод-безопасность @PreAuthorize
     * отклоняет запрос и возвращает 403 Forbidden.
     *
     * Примечание: используется authorities = {"USER"} (роль USER в иерархии ролей ниже ADMIN),
     * чтобы пользователь был аутентифицирован, но не прошёл проверку hasRole('ADMIN').
     */
    @Test
    @WithMockUser(username = "user", authorities = {"USER"})
    void createRatingParametersWithoutAdminRoleReturns403() throws Exception {
        mockMvc.perform(post("/rating_parameters/create")
                        .param("code", "FORBIDDEN_PARAM")
                        .param("description", "Попытка без прав")
                        .param("minValue", "1.0")
                        .param("maxValue", "10.0")
                        .param("coefficient", "0.5")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        // Проверяем, что запись НЕ была сохранена в БД (запрос отклонён до сохранения)
        RatingParameters entity = ratingParametersRepository.findByCode("FORBIDDEN_PARAM");
        Assertions.assertNull(entity, "Запись с кодом FORBIDDEN_PARAM не должна быть сохранена при 403");
    }

    /**
     * Тест create в RatingParametersController: отправка null/пустых значений (code или description)
     * приводит к ошибке 400 Bad Request и запись НЕ сохраняется в БД.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createRatingParametersWithNullValuesReturns400() throws Exception {
        // Не передаём параметры code и description вовсе -> Spring оставит их null в RatingParameters
        mockMvc.perform(post("/rating_parameters/create")
                        .param("minValue", "1.0")
                        .param("maxValue", "10.0")
                        .param("coefficient", "0.5")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // Проверяем, что запись не попала в БД (запрос отклонён до сохранения)
        RatingParameters entity = ratingParametersRepository.findByCode(null);
        Assertions.assertNull(entity, "Запись с null кодом не должна быть сохранена при 400");
    }

    /**
     * Тест create в RatingParametersController: повторное создание параметра с уже существующим
     * кодом отклоняется с 409 Conflict (проверка уникальности через repository.existsByCode),
     * дубль НЕ сохраняется — количество записей с этим кодом остаётся равным одному.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createRatingParametersWithDuplicateCodeReturns409() throws Exception {
        // Сначала успешно создаём запись с кодом DUP_PARAM
        RatingParameters first = new RatingParameters(null, "DUP_PARAM", "Первый параметр", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(first);

        // Повторный POST с тем же кодом должен быть отклонён с 409 Conflict
        mockMvc.perform(post("/rating_parameters/create")
                        .param("code", "DUP_PARAM")
                        .param("description", "Попытка дубликата")
                        .param("minValue", "1.0")
                        .param("maxValue", "10.0")
                         .param("coefficient", "0.5")
                          .with(csrf()))
                  .andExpect(status().isConflict());

        // Запись с кодом DUP_PARAM осталась ровно одна (дубль не сохранён)
        RatingParameters entity = ratingParametersRepository.findByCode("DUP_PARAM");
        Assertions.assertNotNull(entity, "Первоначальная запись с кодом DUP_PARAM должна остаться в БД");
        Assertions.assertEquals(1L, ratingParametersRepository.count(), "Дубликат записи с тем же code не должен быть сохранён");
    }

    /**
     * Тест create в RatingParametersController: пустая строка в code ("") не проходит Bean Validation
     * (@NotBlank) и приводит к 400 Bad Request, запись НЕ сохраняется в БД.
     * Отличие от теста с null-значениями: параметр передаётся, но с пустым значением —
     * @NotBlank ловит именно ""/пробелы, а не только отсутствие поля.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createRatingParametersWithBlankCodeReturns400() throws Exception {
        mockMvc.perform(post("/rating_parameters/create")
                        .param("code", "")
                        .param("description", "Параметр с пустым кодом")
                        .param("minValue", "1.0")
                        .param("maxValue", "10.0")
                        .param("coefficient", "0.5")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // Запись не попала в БД (валидация отклонила запрос до сохранения)
        Assertions.assertEquals(0L, ratingParametersRepository.count(),
                "Запись с пустым кодом не должна быть сохранена при 400");
    }

    /**
     * Тест create в RatingParametersController: пустая строка в description ("") не проходит
     * Bean Validation (@NotBlank) и приводит к 400 Bad Request, запись НЕ сохраняется в БД.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createRatingParametersWithBlankDescriptionReturns400() throws Exception {
        mockMvc.perform(post("/rating_parameters/create")
                        .param("code", "BLANK_DESC_PARAM")
                        .param("description", "")
                        .param("minValue", "1.0")
                        .param("maxValue", "10.0")
                        .param("coefficient", "0.5")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // Запись не попала в БД (валидация отклонила запрос до сохранения)
        RatingParameters entity = ratingParametersRepository.findByCode("BLANK_DESC_PARAM");
        Assertions.assertNull(entity, "Запись с пустым description не должна быть сохранена при 400");
    }

    /**
     * Тест create в RatingParametersController: НЕАВТОРИЗОВАННЫЙ запрос (без @WithMockUser)
     * отклоняется Spring Security ДО метода-контроллера и редиректится на страницу логина /login,
     * запись НЕ сохраняется в БД. Отличие от теста с аутентифицированным USER: пользователь
     * вообще не аутентифицирован, поэтому проверка hasRole('ADMIN') даже не выполняется —
     * anyRequest().authenticated() пускает запрос через formLogin().loginPage("/login"),
     * который отдаёт Thymeleaf-шаблон login.html (Spring MVC view).
     */
    @Test
    void createRatingParametersWithoutAuthenticationRedirectsToLogin() throws Exception {
        // Запрос выполняется БЕЗ @WithMockUser. Поведение соответствует реальному приложению:
        // в SecurityConfig anonymous() НЕ вызывается явно, но по умолчанию (Spring Security 6.x)
        // AnonymousAuthenticationFilter ВКЛЮЧЁН и подставляет AnonymousAuthenticationToken, когда
        // authentication == null. FilterSecurityInterceptor бросает AccessDeniedException (аноним не прошёл
        // anyRequest().authenticated()), а ExceptionTranslationFilter через AuthenticationTrustResolver
        // определяет анонима и вызывает authenticationEntryPoint -> 302 /login.
        // .with(csrf()) обязателен: CsrfFilter проверяет токен ДО ExceptionTranslationFilter,
        // поэтому без токена неавторизованный POST получает 403 от CSRF-фильтра, а не редирект.
        org.springframework.test.web.servlet.MvcResult postUnauth = mockMvc.perform(
                        post("/rating_parameters/create")
                                .param("code", "UNAUTH_PARAM")
                                .param("description", "Попытка без аутентификации")
                                .param("minValue", "1.0")
                                .param("maxValue", "10.0")
                                .param("coefficient", "0.5")
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        // LoginUrlAuthenticationEntryPoint может вернуть полный URL (http://localhost/login),
        // поэтому проверяем, что Location содержит "/login".
        String location = postUnauth.getResponse().getHeader("Location");
        Assertions.assertNotNull(location, "Неавторизованный POST /create должен вернуть 302 с Location");
        Assertions.assertTrue(location.contains("/login"),
                "Ожидался редирект на /login, фактический Location: " + location);

        // Запись не попала в БД (запрос отклонён на уровне security-фильтров)
        RatingParameters entity = ratingParametersRepository.findByCode("UNAUTH_PARAM");
        Assertions.assertNull(entity, "Запись с кодом UNAUTH_PARAM не должна быть сохранена без аутентификации");

        // Страница логина отдаётся как обычный Spring MVC view (Thymeleaf-шаблон login.html)
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .contentTypeCompatibleWith("text/html"));

        // Контроль: GET /rating_parameters/create без аутентификации тоже редиректит на /login
        org.springframework.test.web.servlet.MvcResult getUnauth = mockMvc.perform(get("/rating_parameters/create"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String getLocation = getUnauth.getResponse().getHeader("Location");
        Assertions.assertNotNull(getLocation, "Неавторизованный GET /create должен вернуть 302 с Location");
        Assertions.assertTrue(getLocation.contains("/login"),
                "Ожидался редирект на /login, фактический Location: " + getLocation);
    }

    /**
     * Тест create в RatingParametersController: передача нечисловой строки ("abc") в числовое поле
     * minValue приводит к 400 Bad Request (Spring не может сконвертировать значение в float,
     * выбрасывается TypeMismatchException), запись НЕ сохраняется в БД.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createRatingParametersWithInvalidNumericValueReturns400() throws Exception {
        mockMvc.perform(post("/rating_parameters/create")
                        .param("code", "BAD_NUMBER_PARAM")
                        .param("description", "Параметр с невалидным числом")
                        .param("minValue", "abc")
                        .param("maxValue", "10.0")
                        .param("coefficient", "0.5")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // Запись не попала в БД (ошибка конвертации отклонила запрос до сохранения)
        RatingParameters entity = ratingParametersRepository.findByCode("BAD_NUMBER_PARAM");
        Assertions.assertNull(entity, "Запись с невалидным minValue не должна быть сохранена при 400");
    }

    /**
     * Тест create в RatingParametersController: minValue > maxValue отклоняется с 400 Bad Request
     * (class-level constraint @MinNotGreaterThanMax на RatingParameters), запись НЕ сохраняется в БД.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createRatingParametersWithMinGreaterThanMaxReturns400() throws Exception {
        mockMvc.perform(post("/rating_parameters/create")
                        .param("code", "MIN_GT_MAX_PARAM")
                        .param("description", "Параметр с minValue > maxValue")
                        .param("minValue", "15.0")
                        .param("maxValue", "10.0")
                        .param("coefficient", "0.5")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // Запись не попала в БД (валидация диапазона отклонила запрос до сохранения)
        RatingParameters entity = ratingParametersRepository.findByCode("MIN_GT_MAX_PARAM");
        Assertions.assertNull(entity, "Запись с minValue > maxValue не должна быть сохранена при 400");
    }

    /**
     * Тест create в RatingParametersController: minValue == maxValue — допустимый случай (200/3xx),
     * запись сохраняется в БД.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createRatingParametersWithMinEqualMaxSucceeds() throws Exception {
        mockMvc.perform(post("/rating_parameters/create")
                        .param("code", "MIN_EQ_MAX_PARAM")
                        .param("description", "Параметр с minValue == maxValue")
                        .param("minValue", "5.0")
                        .param("maxValue", "5.0")
                        .param("coefficient", "0.5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        RatingParameters entity = ratingParametersRepository.findByCode("MIN_EQ_MAX_PARAM");
        Assertions.assertNotNull(entity, "Запись с minValue == maxValue должна быть сохранена в БД");
    }

    /**
     * Тест create в RatingParametersController: после успешного создания параметра и редиректа
     * на /rating_parameters/all новый параметр отображается на странице списка (содержится в HTML).
     * Закрывает полный пользовательский сценарий: создание -> редирект -> видимость результата.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createdRatingParametersAppearsOnAllPageAfterRedirect() throws Exception {
        mockMvc.perform(post("/rating_parameters/create")
                        .param("code", "VISIBLE_PARAM")
                        .param("description", "Видимый на списке параметр")
                        .param("minValue", "1.0")
                        .param("maxValue", "10.0")
                        .param("coefficient", "0.5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating_parameters/all"));

        // После редиректа новый код параметра должен отобразиться на странице /all
        mockMvc.perform(get("/rating_parameters/all").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("VISIBLE_PARAM")));

        // Контроль: запись действительно в БД с ожидаемыми полями
        RatingParameters entity = ratingParametersRepository.findByCode("VISIBLE_PARAM");
        Assertions.assertNotNull(entity, "Запись с кодом VISIBLE_PARAM должна быть сохранена");
    }

    /**
     * Тест SimpleErrorController: /error доступен НЕАВТОРИЗОВАННОМУ пользователю и при атрибуте
     * ERROR_STATUS_CODE=400 отдаёт HTML-страницу (400page.html).
     * Эмулируем контейнерную диспетчеризацию ошибки, устанавливая request-атрибуты.
     * Отсутствие @WithMockUser проверяет, что /error не требует аутентификации (permitAll в SecurityConfig).
     */
    @Test
    void errorPageFor400ReturnsHtmlWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 400)
                        .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Invalid input"))
                // Неавторизованный пользователь должен получать HTML-страницу ошибки (400page.html), а не редирект на /login
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }
}
