package com.example.digitalplatform.controller;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты SimpleErrorController: проверка, что /error возвращает корректные HTML-страницы
 * (400page, 403page, 404page, 409page, 500page) при соответствующих значениях ERROR_STATUS_CODE.
 *
 * ВАЖНО о MockMvc: когда исключение бросается ВНУТРИ контроллера (TypeMismatchException из
 * @RequestParam UUID или ResponseStatusException(404) из сервиса), в MockMvc его перехватывает
 * DefaultHandlerExceptionResolver и отвечает статусом с ПУСТЫМ телом — он НЕ выполняет реальный
 * error-dispatch/forward на /error, поэтому SimpleErrorController в этот момент НЕ рендерит HTML.
 * Поэтому корректность самих HTML-страниц по содержимому проверяется прямым GET /error (с атрибутом
 * ERROR_STATUS_CODE) ниже. Статусы 400/404 со стороны реального контроллера /edit покрыты тестами
 * в RatingParametersControllerEditSaveTest.
 */
@SpringBootTest(classes = com.example.digitalplatform.DigitalPlatformApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SimpleErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 400 -> HTML-страница 400page.html: проверка по УНИКАЛЬНОМУ содержимому страницы, а не только
     * статусу (маркеры "Bad Request" и "try again" есть только в 400page.html, что отличает её от
     * остальных страниц ошибок).
     */
    @Test
    void errorPageFor400ReturnsHtml() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 400)
                        .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Invalid input"))
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("<html lang")))
                // Уникальные маркеры 400page.html (отличают от 403/404/500):
                .andExpect(content().string(containsString("Bad Request!")))
                .andExpect(content().string(containsString("try again")));
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
     * 404 -> HTML-страница 404page.html: проверка по УНИКАЛЬНОМУ содержимому страницы (маркеры
     * "Page not found" и "doesn't exist" есть только в 404page.html), а не только статусу.
     */
    @Test
    void errorPageFor404ReturnsHtml() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                        .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Not found"))
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("<html lang")))
                // Уникальные маркеры 404page.html (отличают от 400/403/500).
                // Примечание: в шаблоне используется типографская апостроф (you’re / doesn’t) — U+2019,
                // поэтому ASCII-маркер "doesn't exist" не совпадает; проверяем только надёжные маркеры.
                .andExpect(content().string(containsString("Page not found.")));
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
