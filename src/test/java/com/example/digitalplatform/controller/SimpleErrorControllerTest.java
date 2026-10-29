package com.example.digitalplatform.controller;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты SimpleErrorController: проверка, что /error возвращает корректные HTML-страницы
 * (400page, 403page, 404page, 409page, 500page) при соответствующих значениях ERROR_STATUS_CODE.
 */
@SpringBootTest(classes = com.example.digitalplatform.DigitalPlatformApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SimpleErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 400 -> HTML-страница 400page.html (содержит "Bad Request" и заголовок "400").
     */
    @Test
    void errorPageFor400ReturnsHtml() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 400)
                        .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Invalid input"))
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<html lang")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("400")));
    }

    /**
     * 403 -> HTML-страница 403page.html (содержит "Forbidden" и заголовок "403").
     */
    @Test
    void errorPageFor403ReturnsHtml() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403)
                        .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Access denied"))
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<html lang")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("403")));
    }

    /**
     * 404 -> HTML-страница 404page.html (содержит "Not Found" и заголовок "404").
     */
    @Test
    void errorPageFor404ReturnsHtml() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                        .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Not found"))
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<html lang")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("404")));
    }

    /**
     * 409 -> HTML-страница 409page.html (содержит "Conflict" и заголовок "409").
     */
    @Test
    void errorPageFor409ReturnsHtml() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 409)
                        .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Conflict"))
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<html lang")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("409")));
    }

    /**
     * 500 -> HTML-страница 500page.html (содержит "Internal Server Error" и заголовок "500").
     */
    @Test
    void errorPageFor500ReturnsHtml() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
                        .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Internal server error"))
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<html lang")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("500")));
    }

    /**
     * Неизвестный статус (418) -> HTML-страница error.html (общая страница ошибки).
     */
    @Test
    void errorPageForUnknownStatusReturnsHtml() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 418)
                        .requestAttr(RequestDispatcher.ERROR_MESSAGE, "I'm a teapot"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<html lang")));
    }

    /**
     * Отсутствующий атрибут ERROR_STATUS_CODE -> HTML-страница error.html (общая страница ошибки).
     */
    @Test
    void errorPageWithoutStatusAttributeReturnsHtml() throws Exception {
        mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<html lang")));
    }
}
