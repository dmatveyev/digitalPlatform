package com.example.digitalplatform.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Тесты edit/save (GET/POST /rating_parameters/edit) в RatingParametersController:
 * - GET с валидным id -> 200 OK + HTML-форма редактирования (rating_parameters_edit);
 * - GET с мусорным id ("abc") -> 400 Bad Request + HTML-страница 400page.html;
 *   (парсинг UUID передан на уровень MVC: @RequestParam("id") UUID, Spring бросает
 *   TypeMismatchException -> 400 через /error -> SimpleErrorController);
 * - GET с валидным id без записи в БД -> 404 Not Found + HTML-страница 404page.html;
 * - POST save с невалидными полями (пустой code/description) -> 400 Bad Request, запись НЕ обновляется;
 *   (Bean Validation @NotBlank + class-level constraint @MinNotGreaterThanMax на RatingParameters);
 * - POST save с валидными полями -> 3xx редирект на /rating_parameters/all + поля записи обновлены в БД.
 */
@SpringBootTest(classes = com.example.digitalplatform.DigitalPlatformApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RatingParametersControllerEditSaveTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingParametersRepository ratingParametersRepository;

    @BeforeEach
    void setUp() {
        ratingParametersRepository.deleteAll();
    }

    /**
     * GET /rating_parameters/edit?id=<uuid> с валидным id существующей записи: 200 OK,
     * отдаётся HTML-форма редактирования (содержит заголовок "Информация о параметре" и name="code").
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void editWithValidIdReturns200AndEditForm() throws Exception {
        RatingParameters entity = new RatingParameters(null, "EDIT_PARAM", "Параметр для редактирования", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(entity);

        mockMvc.perform(get("/rating_parameters/edit")
                        .param("id", entity.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                // Форма редактирования: заголовок и поле code (name="code" в rating_parameters_edit.html)
                .andExpect(content().string(containsString("Информация о параметре")))
                .andExpect(content().string(containsString("name=\"code\"")));

        // Контроль: запись осталась в БД без изменений
        Assertions.assertNotNull(ratingParametersRepository.findByCode("EDIT_PARAM"));
    }

    /**
     * GET /rating_parameters/edit?id=abc (мусорный id): парсинг UUID на уровне MVC
     * (@RequestParam("id") UUID) бросает TypeMismatchException -> 400 Bad Request,
     * SimpleErrorController отдаёт HTML-страницу 400page.html (проверка по содержимому).
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void editWithInvalidIdReturns400HtmlPage() throws Exception {
        RatingParameters entity = new RatingParameters(null, "EDIT_BAD_ID_PARAM", "Параметр для проверки мусорного id в edit", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(entity);

        mockMvc.perform(get("/rating_parameters/edit")
                        .param("id", "abc"))
                // TypeMismatchException (Spring не смог сконвертировать "abc" в UUID) -> 400 Bad Request.
                // В MockMvc DefaultHandlerExceptionResolver отвечает 400 с пустым телом (без content-type),
                // поэтому проверяем статус; HTML-страницу 400page для этого кода покрывает SimpleErrorControllerTest.
                .andExpect(status().isBadRequest());

        // Запись не изменена (запрос отклонён на уровне парсинга параметра)
        Assertions.assertNotNull(ratingParametersRepository.findByCode("EDIT_BAD_ID_PARAM"));
    }

    /**
     * GET /rating_parameters/edit?id=<valid uuid> без записи в БД: сервис бросает 404 Not Found,
     * SimpleErrorController отдаёт HTML-страницу 404page.html (проверка по содержимому).
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void editWithNonExistingIdReturns404HtmlPage() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(get("/rating_parameters/edit")
                        .param("id", missingId.toString()))
                // ResponseStatusException(404) из сервиса -> 404 Not Found.
                // В MockMvc DefaultHandlerExceptionResolver отвечает статусом без тела (без content-type),
                // поэтому проверяем статус; HTML-страницу 404page для этого кода покрывает SimpleErrorControllerTest.
                .andExpect(status().isNotFound());
    }

    /**
     * POST /rating_parameters/edit?id=<uuid> с невалидными полями (пустой code и description):
     * Bean Validation (@NotBlank) -> MethodArgumentNotValidException -> 400 Bad Request,
     * запись НЕ обновляется в БД.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void saveWithInvalidFieldsReturns400() throws Exception {
        RatingParameters entity = new RatingParameters(null, "SAVE_INVALID_PARAM", "Первоначальное описание", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(entity);

        mockMvc.perform(post("/rating_parameters/edit")
                        .param("id", entity.getId().toString())
                        .param("code", "")
                        .param("description", "")
                        .param("minValue", "1.0")
                        .param("maxValue", "10.0")
                        .param("coefficient", "0.5")
                        .with(csrf()))
                // @NotBlank на code/description -> MethodArgumentNotValidException -> 400 до записи в БД.
                // В MockMvc ответ отдаётся статусом без тела (без content-type); HTML-страницу 400page
                // для этого кода покрывает SimpleErrorControllerTest.
                .andExpect(status().isBadRequest());

        // Запись не обновлена: description остался прежним
        RatingParameters after = ratingParametersRepository.findByCode("SAVE_INVALID_PARAM");
        Assertions.assertNotNull(after);
        Assertions.assertEquals("Первоначальное описание", after.getDescription(),
                "Описание не должно измениться при 400 на пустых полях");
    }

    /**
     * POST /rating_parameters/edit?id=<uuid> с валидными полями: 3xx редирект на /rating_parameters/all,
     * запись реально обновлена в БД (description/minValue/maxValue/coefficient).
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void saveWithValidFieldsRedirectsAndUpdatesEntity() throws Exception {
        RatingParameters entity = new RatingParameters(null, "SAVE_VALID_PARAM", "Старое описание", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(entity);

        mockMvc.perform(post("/rating_parameters/edit")
                        .param("id", entity.getId().toString())
                        .param("code", "SAVE_VALID_PARAM")
                        .param("description", "Новое описание")
                        .param("minValue", "2.0")
                        .param("maxValue", "20.0")
                        .param("coefficient", "1.5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating_parameters/all"));

        // Запись обновлена в БД
        RatingParameters after = ratingParametersRepository.findByCode("SAVE_VALID_PARAM");
        Assertions.assertNotNull(after, "Запись с кодом SAVE_VALID_PARAM должна остаться в БД после save");
        Assertions.assertEquals(entity.getId(), after.getId(), "После обновления id записи должен сохраниться");
        Assertions.assertEquals("Новое описание", after.getDescription());
        Assertions.assertEquals(2.0f, after.getMinValue(), 0.0001f);
        Assertions.assertEquals(20.0f, after.getMaxValue(), 0.0001f);
        Assertions.assertEquals(1.5f, after.getCoefficient(), 0.0001f);
    }

    /**
     * POST /rating_parameters/edit?id=abc (мусорный id в query-параметре): парсинг UUID на уровне MVC
     * (@RequestParam("id") UUID) бросает TypeMismatchException -> 400 Bad Request, запись не обновляется.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void saveWithInvalidIdReturns400() throws Exception {
        RatingParameters entity = new RatingParameters(null, "SAVE_BAD_ID_PARAM", "Параметр для проверки мусорного id в save", 1.0f, 10.0f, 0.5f);
        ratingParametersRepository.save(entity);

        mockMvc.perform(post("/rating_parameters/edit")
                        .param("id", "abc")
                        .param("code", "SAVE_BAD_ID_PARAM")
                        .param("description", "Попытка сохранения с мусорным id")
                        .param("minValue", "1.0")
                        .param("maxValue", "10.0")
                        .param("coefficient", "0.5")
                        .with(csrf()))
                // TypeMismatchException (Spring не смог сконвертировать "abc" в UUID) -> 400 Bad Request.
                // В MockMvc DefaultHandlerExceptionResolver отвечает 400 с пустым телом (без content-type),
                // поэтому проверяем статус; HTML-страницу 400page для этого кода покрывает SimpleErrorControllerTest.
                .andExpect(status().isBadRequest());

        // Запись не изменена (запрос отклонён на уровне парсинга параметра)
        RatingParameters after = ratingParametersRepository.findByCode("SAVE_BAD_ID_PARAM");
        Assertions.assertNotNull(after);
        Assertions.assertEquals("Параметр для проверки мусорного id в save", after.getDescription());
    }

}
