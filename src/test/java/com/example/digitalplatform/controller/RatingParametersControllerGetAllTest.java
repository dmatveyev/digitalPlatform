package com.example.digitalplatform.controller;

import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.example.digitalplatform.DigitalPlatformApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RatingParametersControllerGetAllTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingParametersRepository ratingParametersRepository;

    @BeforeEach
    void setUp() {
        ratingParametersRepository.deleteAll();
    }

    /**
     * Тест getAll в RatingParametersController:
     * GET /rating_parameters/all возвращает страницу "rating_parameters/rating_parameters"
     * с атрибутом model "pageItem", содержащим сохранённые записи и корректный totalElements.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllReturnsPageWithSavedParameters() throws Exception {
        ratingParametersRepository.save(new RatingParameters(null, "PARAM_1", "Первый параметр", 0f, 100f, 1.5f));
        ratingParametersRepository.save(new RatingParameters(null, "PARAM_2", "Второй параметр", 0f, 50f, 0.75f));

        // Один вызов mockMvc.perform: проверки + сохранение результата
        MvcResult result = mockMvc.perform(get("/rating_parameters/all"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("pageItem"))
                .andReturn();

        // Проверяем содержимое model: totalElements отражает все записи в БД
        Object pageItem = result.getModelAndView().getModelMap().get("pageItem");
        Assertions.assertNotNull(pageItem, "Атрибут 'pageItem' отсутствует в model");

        Page<RatingParameters> resultPage = (Page<RatingParameters>) pageItem;
        Assertions.assertEquals(2, resultPage.getTotalElements(),
                "totalElements должен равняться количеству записей в БД");
        Assertions.assertEquals(2, resultPage.getContent().size(),
                "Страница по умолчанию должна содержать обе записи (pageSize=5)");
    }

    /**
     * T1: пустая БД -> 200, totalElements == 0, content пусто.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllWithEmptyDatabaseReturnsEmptyPage() throws Exception {
        MvcResult result = mockMvc.perform(get("/rating_parameters/all"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("pageItem"))
                .andReturn();

        Page<RatingParameters> pageItem = (Page<RatingParameters>) result.getModelAndView().getModelMap().get("pageItem");
        Assertions.assertEquals(0, pageItem.getTotalElements(), "Пустая БД -> totalElements == 0");
        Assertions.assertTrue(pageItem.getContent().isEmpty(), "Пустая БД -> content пуст");
    }

    /**
     * T2: 7 записей, ?page=1&size=5 -> totalElements == 7, content.size() == 5,
     * первые 5 кодов в порядке сортировки по code.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllPaginationFirstPage() throws Exception {
        // Коды намеренно вперемешку: порядок должен определяться сортировкой, а не порядком вставки
        String[] codes = {"G_7", "B_2", "E_5", "A_1", "F_6", "C_3", "D_4"};
        for (String code : codes) {
            ratingParametersRepository.save(new RatingParameters(null, code, "Описание " + code, 0f, 100f, 1.0f));
        }

        MvcResult result = mockMvc.perform(get("/rating_parameters/all").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andReturn();

        Page<RatingParameters> pageItem = (Page<RatingParameters>) result.getModelAndView().getModelMap().get("pageItem");
        Assertions.assertEquals(7, pageItem.getTotalElements(), "totalElements == 7");
        Assertions.assertEquals(5, pageItem.getContent().size(), "Первая страница size=5 содержит 5 записей");

        String[] firstPageCodes = pageItem.getContent().stream().map(RatingParameters::getCode).toArray(String[]::new);
        Assertions.assertArrayEquals(new String[]{"A_1", "B_2", "C_3", "D_4", "E_5"}, firstPageCodes,
                "Первая страница должна содержать 5 первых записей по сортировке code");
    }

    /**
     * T3: 7 записей, ?page=2&size=5 -> totalElements == 7, content.size() == 2 — последние записи по сортировке.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllPaginationLastPagePartial() throws Exception {
        String[] codes = {"G_7", "B_2", "E_5", "A_1", "F_6", "C_3", "D_4"};
        for (String code : codes) {
            ratingParametersRepository.save(new RatingParameters(null, code, "Описание " + code, 0f, 100f, 1.0f));
        }

        MvcResult result = mockMvc.perform(get("/rating_parameters/all").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andReturn();

        Page<RatingParameters> pageItem = (Page<RatingParameters>) result.getModelAndView().getModelMap().get("pageItem");
        Assertions.assertEquals(7, pageItem.getTotalElements(), "totalElements == 7");
        Assertions.assertEquals(2, pageItem.getContent().size(), "Последняя страница содержит оставшиеся 2 записи");

        String[] lastPageCodes = pageItem.getContent().stream().map(RatingParameters::getCode).toArray(String[]::new);
        Assertions.assertArrayEquals(new String[]{"F_6", "G_7"}, lastPageCodes,
                "Вторая страница должна содержать последние записи по сортировке code");
    }

    /**
     * T4: 3 записи, ?page=10&size=5 -> 200 (не 500), totalElements == 3, content пусто.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllPageBeyondTotalReturnsEmptyContent() throws Exception {
        for (int i = 1; i <= 3; i++) {
            ratingParametersRepository.save(new RatingParameters(null, "P_" + i, "Описание " + i, 0f, 100f, 1.0f));
        }

        MvcResult result = mockMvc.perform(get("/rating_parameters/all").param("page", "10").param("size", "5"))
                .andExpect(status().isOk())
                .andReturn();

        Page<RatingParameters> pageItem = (Page<RatingParameters>) result.getModelAndView().getModelMap().get("pageItem");
        Assertions.assertEquals(3, pageItem.getTotalElements(), "totalElements == 3 даже для страницы за пределами данных");
        Assertions.assertTrue(pageItem.getContent().isEmpty(), "Страница за пределами данных -> content пуст");
    }

    /**
     * T5: без параметров page/size -> поведение как page=1, size=5; totalPages корректен при >5 записях.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllWithoutParametersUsesDefaults() throws Exception {
        for (int i = 1; i <= 7; i++) {
            ratingParametersRepository.save(new RatingParameters(null, "P_" + i, "Описание " + i, 0f, 100f, 1.0f));
        }

        MvcResult result = mockMvc.perform(get("/rating_parameters/all"))
                .andExpect(status().isOk())
                .andReturn();

        Page<RatingParameters> pageItem = (Page<RatingParameters>) result.getModelAndView().getModelMap().get("pageItem");
        Assertions.assertEquals(7, pageItem.getTotalElements(), "totalElements == 7");
        Assertions.assertEquals(5, pageItem.getSize(), "Размер страницы по умолчанию == 5");
        // Page.getNumber() — zero-based: первая страница имеет номер 0
        Assertions.assertEquals(0, pageItem.getNumber(), "Номер страницы по умолчанию == 0 (zero-based, первая страница)");
        Assertions.assertEquals(2, pageItem.getTotalPages(), "totalPages при 7 записях и size=5 == 2");
    }

    /**
     * T6: ?page=0&size=5 -> 400 Bad Request.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllWithPageZeroReturns400() throws Exception {
        mockMvc.perform(get("/rating_parameters/all").param("page", "0").param("size", "5"))
                .andExpect(status().isBadRequest());
    }

    /**
     * T7: ?page=1&size=-1 и ?page=1&size=0 -> 400 Bad Request.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllWithNegativeSizeReturns400() throws Exception {
        mockMvc.perform(get("/rating_parameters/all").param("page", "1").param("size", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/rating_parameters/all").param("page", "1").param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    /**
     * T8: ?page=abc -> 400 (TypeMismatchException диспетчеризуется в /error).
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllWithNonNumericPageReturns400() throws Exception {
        mockMvc.perform(get("/rating_parameters/all").param("page", "abc"))
                .andExpect(status().isBadRequest());
    }

    /**
     * T9: аутентифицированный пользователь без роли ADMIN -> 403.
     *
     * Примечание: SecurityConfig использует GrantedAuthorityDefaults("") (пустой префикс ролей),
     * поэтому @PreAuthorize("hasRole('ADMIN')") проверяет право 'ADMIN' без префикса ROLE_.
     * authorities = {"USER"} — пользователь аутентифицирован, но не прошёл hasRole('ADMIN').
     */
    @Test
    @WithMockUser(username = "user", authorities = {"USER"})
    void getAllWithoutAdminRoleReturns403() throws Exception {
        mockMvc.perform(get("/rating_parameters/all"))
                .andExpect(status().isForbidden());
    }

    /**
     * T10: НЕАВТОРИЗОВАННЫЙ запрос (без @WithMockUser) -> 3xx редирект на /login.
     * GET не требует CSRF-токен.
     */
    @Test
    void getAllWithoutAuthenticationRedirectsToLogin() throws Exception {
        MvcResult result = mockMvc.perform(get("/rating_parameters/all"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        Assertions.assertNotNull(location, "Неавторизованный GET /all должен вернуть 3xx с Location");
        Assertions.assertTrue(location.contains("/login"),
                "Ожидался редирект на /login, фактический Location: " + location);
    }

    /**
     * T11: записи с кодами вперемешку (ZED, ALPHA, MID) -> порядок на странице строго по сортировке code.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllSortedByCode() throws Exception {
        ratingParametersRepository.save(new RatingParameters(null, "ZED", "Последний параметр", 0f, 100f, 1.0f));
        ratingParametersRepository.save(new RatingParameters(null, "ALPHA", "Первый параметр", 0f, 100f, 1.0f));
        ratingParametersRepository.save(new RatingParameters(null, "MID", "Средний параметр", 0f, 100f, 1.0f));

        MvcResult result = mockMvc.perform(get("/rating_parameters/all"))
                .andExpect(status().isOk())
                .andReturn();

        Page<RatingParameters> pageItem = (Page<RatingParameters>) result.getModelAndView().getModelMap().get("pageItem");
        String[] codes = pageItem.getContent().stream().map(RatingParameters::getCode).toArray(String[]::new);
        Assertions.assertArrayEquals(new String[]{"ALPHA", "MID", "ZED"}, codes,
                "Страница должна содержать записи строго в порядке сортировки по code");
    }

    /**
     * T12: содержимое страницы (конкретные коды) совпадает с ожидаемым срезом БД при пагинации.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllPageContentMatchesDatabaseOrder() throws Exception {
        String[] codes = {"P_10", "P_2", "P_9", "P_3", "P_8", "P_4"};
        for (String code : codes) {
            ratingParametersRepository.save(new RatingParameters(null, code, "Описание " + code, 0f, 100f, 1.0f));
        }

        // Отсортировано по code: P_10, P_2, P_3, P_4, P_8, P_9 (lexicographic)
        MvcResult result = mockMvc.perform(get("/rating_parameters/all").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andReturn();

        Page<RatingParameters> pageItem = (Page<RatingParameters>) result.getModelAndView().getModelMap().get("pageItem");
        String[] codesOnPage = pageItem.getContent().stream().map(RatingParameters::getCode).toArray(String[]::new);
        Assertions.assertArrayEquals(new String[]{"P_9"}, codesOnPage,
                "Вторая страница size=5 должна содержать ровно последнюю запись отсортированного списка");

        // HTML-отдача: конкретный код присутствует в теле страницы
        MvcResult htmlResult = mockMvc.perform(get("/rating_parameters/all").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertTrue(htmlResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)
                .contains("P_9"), "HTML страницы должен содержать код P_9");
    }
}
