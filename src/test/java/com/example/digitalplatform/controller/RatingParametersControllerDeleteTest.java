package com.example.digitalplatform.controller;

import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.containsString;

/**
 * Тесты delete в RatingParametersController: POST /rating_parameters/delete.
 * Каркас по образцу RatingParametersControllerCreateTest (аннотации, поля, setUp).
 *
 * Эти тесты проверяют НОВОЕ (после рефакторинга) поведение delete, вынесенное в RatingParametersService:
 * - успешное удаление существующей записи -> 3xx редирект на /rating_parameters/all + запись удалена из БД;
 * - удаление несуществующего (валидного) UUID -> 404 Not Found (через /error, SimpleErrorController мапит 404 -> "404page");
 * - невалидный UUID ("abc") -> 400 Bad Request (через /error, SimpleErrorController мапит 400 -> "400page");
 * - POST без роли ADMIN (@WithMockUser(authorities = {"USER"})) -> 403 Forbidden;
 * - неавторизованный POST (без @WithMockUser, с .with(csrf())) -> 3xx редирект на /login.
 */
@SpringBootTest(classes = com.example.digitalplatform.DigitalPlatformApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RatingParametersControllerDeleteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingParametersRepository ratingParametersRepository;

    @BeforeEach
    void setUp() {
        ratingParametersRepository.deleteAll();
    }

    /**
     * Тест delete в RatingParametersController: успешное удаление существующей записи.
     * POST /rating_parameters/delete?id=<uuid> возвращает 3xx редирект на /rating_parameters/all,
     * а запись реально исчезает из БД (findById пуст).
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void deleteExistingRatingParametersRedirectsToAll() throws Exception {
        RatingParameters entity = new RatingParameters(null, "DEL_PARAM", "Параметр для удаления", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(entity);
        UUID id = entity.getId();

        mockMvc.perform(post("/rating_parameters/delete")
                        .param("id", id.toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating_parameters/all"));

        // Проверяем, что запись реально удалена из БД
        Assertions.assertTrue(ratingParametersRepository.findById(id).isEmpty(),
                "Запись с id " + id + " должна быть удалена из БД после delete");
    }

    /**
     * Тест delete в RatingParametersController: удаление НЕСУЩЕСТВУЮЩЕГО (валидного) UUID.
     * НОВОЕ поведение (после рефакторинга): сервис бросает ResponseStatusException(404, "...not found"),
     * который диспетчеризуется в /error -> SimpleErrorController -> HTML-страница 404page.html.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void deleteNonExistingValidUuidReturns404() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(post("/rating_parameters/delete")
                        .param("id", missingId.toString())
                        .with(csrf()))
                // 404 диспетчеризуется в /error, SimpleErrorController мапит 404 -> "404page" (HTML)
                .andExpect(status().isNotFound());

        // БД не изменилась: записи всё ещё нет (удаление не выполнялось)
        Assertions.assertTrue(ratingParametersRepository.findById(missingId).isEmpty(),
                "Отсутствующая запись не должна появиться в БД при 404");
    }

    /**
     * Тест delete в RatingParametersController при отсутствии прав доступа:
     * пользователь аутентифицирован, но не имеет роли ADMIN -> метод-безопасность @PreAuthorize
     * отклоняет запрос и возвращает 403 Forbidden.
     */
    @Test
    @WithMockUser(username = "user", authorities = {"USER"})
    void deleteWithoutAdminRoleReturns403() throws Exception {
        RatingParameters entity = new RatingParameters(null, "FORBIDDEN_DEL_PARAM", "Параметр без прав", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(entity);

        mockMvc.perform(post("/rating_parameters/delete")
                        .param("id", entity.getId().toString())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        // Проверяем, что запись НЕ была удалена (запрос отклонён до удаления)
        Assertions.assertTrue(ratingParametersRepository.findById(entity.getId()).isPresent(),
                "Запись не должна быть удалена при 403");
    }

    /**
     * Тест 403 для аутентифицированного пользователя без роли ADMIN: ExceptionTranslationFilter
     * через настроенный accessDeniedHandler форвардит на /error, SimpleErrorController отдаёт
     * HTML-страницу 403page (проверка по содержимому, а не только статусу).
     */
    @Test
    void deleteWithoutAdminRoleReturns403HtmlPage() throws Exception {
        RatingParameters entity = new RatingParameters(null, "FORBIDDEN_HTML_PARAM", "Параметр для проверки 403 HTML", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(entity);

        mockMvc.perform(post("/rating_parameters/delete")
                        .param("id", entity.getId().toString())
                        .with(user("user").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("<html lang")))
                .andExpect(content().string(containsString("403")));

        Assertions.assertTrue(ratingParametersRepository.findById(entity.getId()).isPresent(),
                "Запись не должна быть удалена при 403");
    }

    /**
     * Тест delete в RatingParametersController: НЕАВТОРИЗОВАННЫЙ запрос (без @WithMockUser)
     * отклоняется Spring Security ДО метода-контроллера и редиректится на страницу логина /login.
     */
    @Test
    void deleteWithoutAuthenticationRedirectsToLogin() throws Exception {
        org.springframework.test.web.servlet.MvcResult postUnauth = mockMvc.perform(
                        post("/rating_parameters/delete")
                                .param("id", UUID.randomUUID().toString())
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        // LoginUrlAuthenticationEntryPoint может вернуть полный URL (http://localhost/login),
        // поэтому проверяем, что Location содержит "/login".
        String location = postUnauth.getResponse().getHeader("Location");
        Assertions.assertNotNull(location, "Неавторизованный POST /delete должен вернуть 302 с Location");
        Assertions.assertTrue(location.contains("/login"),
                "Ожидался редирект на /login, фактический Location: " + location);
    }

    /**
     * Тест delete в RatingParametersController: мусорный id ("abc").
     * НОВОЕ поведение (после рефакторинга): сервис перехватывает IllegalArgumentException из UUID.fromString
     * и бросает ResponseStatusException(400, "Invalid rating parameter id: 'abc'"), который диспетчеризуется
     * в /error -> SimpleErrorController -> HTML-страница 400page.html. Плохой ввод = 400 (единообразие с create/getAll).
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void deleteWithInvalidUuidReturns400() throws Exception {
        // Сначала сохраним запись, чтобы убедиться, что невалидный id её НЕ удаляет.
        RatingParameters entity = new RatingParameters(null, "BAD_ID_PARAM", "Параметр с мусорным id", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(entity);

        mockMvc.perform(post("/rating_parameters/delete")
                        .param("id", "abc")
                        .with(csrf()))
                // 400 диспетчеризуется в /error, SimpleErrorController мапит 400 -> "400page" (HTML)
                .andExpect(status().isBadRequest());

        // Запись НЕ удалена (невалидный id отклонён до удаления)
        Assertions.assertTrue(ratingParametersRepository.findById(entity.getId()).isPresent(),
                "Запись не должна быть удалена при 400 на мусорном id");
    }

    /**
     * Тест delete в RatingParametersController: пустой id (id=).
     * НОВОЕ поведение после добавления Bean Validation (@NotBlank в RatingParametersDeleteRequest):
     * Spring выбрасывает MethodArgumentNotValidException -> 400 Bad Request, запись не удаляется.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void deleteWithBlankIdReturns400() throws Exception {
        RatingParameters entity = new RatingParameters(null, "BLANK_ID_PARAM", "Параметр с пустым id", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(entity);

        mockMvc.perform(post("/rating_parameters/delete")
                        .param("id", "")
                        .with(csrf()))
                // Bean Validation (@NotBlank) возвращает 400 до вызова сервиса
                .andExpect(status().isBadRequest());

        // Запись НЕ удалена (валидация не пройдена)
        Assertions.assertTrue(ratingParametersRepository.findById(entity.getId()).isPresent(),
                "Запись не должна быть удалена при 400 на пустом id");
    }

    /**
     * Тест delete в RatingParametersController: параметр id отсутствует.
     * НОВОЕ поведение после добавления Bean Validation (@NotBlank в RatingParametersDeleteRequest):
     * Spring мапит отсутствующий параметр как null, @NotBlank -> 400 Bad Request, запись не удаляется.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void deleteWithoutIdParameterReturns400() throws Exception {
        RatingParameters entity = new RatingParameters(null, "MISSING_ID_PARAM", "Параметр без id в запросе", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(entity);

        mockMvc.perform(post("/rating_parameters/delete")
                        .with(csrf()))
                // Bean Validation (@NotBlank) возвращает 400 до вызова сервиса
                .andExpect(status().isBadRequest());

        // Запись НЕ удалена (валидация не пройдена)
        Assertions.assertTrue(ratingParametersRepository.findById(entity.getId()).isPresent(),
                "Запись не должна быть удалена при 400 на отсутствующем id");
    }

    /**
     * Тест формы rating_parameters/rating_parameters: на странице списка параметров присутствует кнопка удаления.
     * GET /rating_parameters/all возвращает 200 OK, а в HTML-ответе присутствуют форма удаления и текст кнопки «Удалить».
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void ratingParametersPageContainsDeleteButton() throws Exception {
        RatingParameters entity = new RatingParameters(null, "DELETE_BUTTON_PARAM", "Параметр для проверки кнопки удаления", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(entity);

        org.springframework.test.web.servlet.MvcResult mvcResult = mockMvc.perform(
                        get("/rating_parameters/all"))
                .andExpect(status().isOk())
                .andReturn();

        String html = mvcResult.getResponse().getContentAsString();
        // Thymeleaf подставляет th:action как обычный атрибут action, поэтому проверяем финальный HTML.
        Assertions.assertTrue(html.contains("action=\"/rating_parameters/delete\""),
                "На странице списка параметров должна присутствовать форма удаления с action=\"/rating_parameters/delete\"");
        Assertions.assertTrue(html.contains("value=\"Удалить\""),
                "На странице списка параметров должна присутствовать кнопка «Удалить» (value=\"Удалить\")");
    }

}
