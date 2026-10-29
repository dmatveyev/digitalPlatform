package com.example.digitalplatform;

import com.example.digitalplatform.controller.RatingParametersController;
import com.example.digitalplatform.service.RatingParametersService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;

/**
 * Проверка загрузки тестового Spring-контекста приложения.
 * Загружает полный контекст (DigitalPlatformApplication, профиль "test") и проверяет,
 * что ключевые бины зарегистрированы: контроллер параметров рейтинга, сервис CRUD
 * и UserDetailsService (spring-security). Пустой метод из исходного каркаса заменён
 * на осмысленную проверку наличия бина.
 */
@SpringBootTest(classes = DigitalPlatformApplication.class)
@ActiveProfiles("test")
class ContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RatingParametersController ratingParametersController;

    @Autowired
    private RatingParametersService ratingParametersService;

    /**
     * Контекст загружен: основные бины присутствуют и не null.
     */
    @Test
    void contextLoadsAndContainsKeyBeans() {
        Assertions.assertNotNull(applicationContext, "ApplicationContext должен быть доступен в тесте");

        // Ключевые бины приложения зарегистрированы (инжектились через @Autowired — иначе тест не собрался бы)
        Assertions.assertNotNull(ratingParametersController, "Бин RatingParametersController должен существовать");
        Assertions.assertNotNull(ratingParametersService, "Бин RatingParametersService должен существовать");

        // UserDetailsService обязателен для spring-security (MyUserDetailService)
        Assertions.assertDoesNotThrow(
                () -> applicationContext.getBean(UserDetailsService.class),
                "В контексте должен быть бин UserDetailsService");
    }
}
