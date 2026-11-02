package com.example.digitalplatform.controller.account;

import com.example.digitalplatform.db.model.Role;
import com.example.digitalplatform.db.model.RoleType;
import org.springframework.transaction.annotation.Transactional;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.repository.RoleRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты AccountController: GET/POST /accounts/create.
 *
 * Все позитивные кейсы выполняются с {@code @WithMockUser(authorities = {"ADMIN"})}:
 * приложение использует GrantedAuthorityDefaults("") (пустой префикс ролей), поэтому
 * @PreAuthorize("hasRole('ADMIN')") проверяет право 'ADMIN' без префикса ROLE_.
 *
 * Bean Validation на UserAccountDto активен с Этапа 2: «пустой userName/пароль -> 400».
 * Контракт «дубликат login -> 409» активирован с Этапа 3.1 (ResponseStatusException(409)).
 */
@SpringBootTest(classes = com.example.digitalplatform.DigitalPlatformApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerCreateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Роли загружаются из БД один раз на весь класс (они неизменны в тестах),
     * чтобы createUser не ходил в базу за ролью при каждом вызове.
     */
    private Map<RoleType, Role> rolesByCode;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    /**
     * Читает сущности через отдельную транзакцию: после завершения MockMvc-запроса (и его
     * транзакции) роль у User — LAZY-прокси, инициализация которого вне открытой сессии
     * падает с LazyInitializationException. Отдельная транзакция даёт Hibernate новую сессию.
     */
    private <T> T inNewTransaction(java.util.function.Supplier<T> action) {
        return new org.springframework.transaction.support.TransactionTemplate(transactionManager)
                .execute(status -> action.get());
    }

    @BeforeEach
    void setUp() {
        // Очищаем таблицу users (включая seed из data.sql), чтобы каждый тест начинался с пустой таблицы.
        userRepository.deleteAll();
        if (rolesByCode == null) {
            rolesByCode = roleRepository.findAll().stream()
                    .collect(Collectors.toMap(Role::getCode, Function.identity()));
        }
    }

    @Test

    private User createUser(String login, RoleType roleCode) {
        User user = new User();
        user.setLogin(login);
        // Не хешируем пароль: для проверки сохранения в тестах значение пароля не используется.
        user.setPassword("plain-password");
        user.setEnabled(true);
        user.setRole(rolesByCode.get(roleCode));
        return user;
    }

    private void postCreate(String userName, String password, RoleType role) throws Exception {
        mockMvc.perform(post("/accounts/create")
                        .param("userName", userName)
                        .param("password", password)
                        .param("roleCode", role.name())
                        .with(csrf()));
    }

    /**
     * T1: GET /accounts/create -> 200, модель содержит userData (пустой UserAccountDto) и roles.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getCreateFormReturnsAttributes() throws Exception {
        mockMvc.perform(get("/accounts/create"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userData"))
                .andExpect(model().attributeExists("roles"));

        Object rolesAttr = mockMvc.perform(get("/accounts/create"))
                .andReturn()
                .getModelAndView()
                .getModelMap()
                .get("roles");
        Assertions.assertNotNull(rolesAttr, "Атрибут 'roles' должен присутствовать в модели");
    }

    /**
     * T2: POST /accounts/create с новым login -> 3xx редирект на /accounts/all, пользователь сохранён.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createAccountSuccessRedirectsAndSaves() throws Exception {
        mockMvc.perform(post("/accounts/create")
                        .param("userName", "new_user_1")
                        .param("password", "Secret#123")
                        .param("roleCode", RoleType.STUDENT.name())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts/all"));

        // Инициализацию LAZY-прокси роли читаем внутри отдельной транзакции (открытой сессии).
        RoleType savedRoleCode = inNewTransaction(() -> {
            User saved = userRepository.findByLogin("new_user_1");
            Assertions.assertNotNull(saved, "Пользователь new_user_1 должен быть сохранён в БД");
            return saved.getRole().getCode();
        });
        Assertions.assertEquals(RoleType.STUDENT, savedRoleCode,
                "У сохранённого пользователя должна быть роль STUDENT из формы");
    }

    /**
     * T3: POST с пустым userName -> 400 Bad Request (Bean Validation @NotBlank на UserAccountDto, Этап 2).
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createAccountWithBlankUserNameReturns400() throws Exception {
        mockMvc.perform(post("/accounts/create")
                        .param("userName", "")
                        .param("password", "Secret#123")
                        .param("roleCode", RoleType.STUDENT.name())
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        Assertions.assertEquals(0, userRepository.count(),
                "Пользователь с пустым userName не должен быть сохранён при 400");
    }

    /**
     * T4: POST с пустым password -> 400 Bad Request (Bean Validation @NotBlank на UserAccountDto, Этап 2).
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createAccountWithBlankPasswordReturns400() throws Exception {
        mockMvc.perform(post("/accounts/create")
                        .param("userName", "blank_pass_user")
                        .param("password", "")
                        .param("roleCode", RoleType.STUDENT.name())
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        Assertions.assertEquals(0, userRepository.count(),
                "Пользователь с пустым паролем не должен быть сохранён при 400");
    }

    /**
     * T5 (целевой контракт после Этапа 3.1): POST с дублирующимся login -> 409 Conflict.
     * До рефакторинга registerNewUserAccount логирует ошибку и молча возвращает существующего
     * пользователя, а контроллер редиректит как при успехе — пользователь не получает обратную связь.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createAccountWithDuplicateLoginReturns409() throws Exception {
        userRepository.save(createUser("dup_login_user", RoleType.STUDENT));

        mockMvc.perform(post("/accounts/create")
                        .param("userName", "dup_login_user")
                        .param("password", "Secret#123")
                        .param("roleCode", RoleType.TEACHER.name())
                        .with(csrf()))
                .andExpect(status().isConflict());

        Assertions.assertEquals(1, userRepository.count(),
                "Дубликат с тем же login не должен быть сохранён");
    }

    /**
     * T6: аутентифицированный пользователь без роли ADMIN -> 403 (метод-безопасность @PreAuthorize).
     */
    @Test
    @WithMockUser(username = "user", authorities = {"USER"})
    void createAccountWithoutAdminRoleReturns403() throws Exception {
        mockMvc.perform(post("/accounts/create")
                        .param("userName", "forbidden_user")
                        .param("password", "Secret#123")
                        .param("roleCode", RoleType.STUDENT.name())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        Assertions.assertNull(userRepository.findByLogin("forbidden_user"),
                "Пользователь не должен быть сохранён при 403");
    }

    /**
     * T7: НЕАВТОРИЗОВАННЫЙ запрос (без @WithMockUser) -> 3xx редирект на /login.
     */
    @Test
    void createAccountWithoutAuthenticationRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/accounts/create")
                        .param("userName", "unauth_user")
                        .param("password", "Secret#123")
                        .param("roleCode", RoleType.STUDENT.name())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        String location = mockMvc.perform(post("/accounts/create")
                        .param("userName", "unauth_user")
                        .param("password", "Secret#123")
                        .param("roleCode", RoleType.STUDENT.name())
                        .with(csrf()))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        Assertions.assertNotNull(location, "Неавторизованный POST /accounts/create должен вернуть 3xx с Location");
        Assertions.assertTrue(location.contains("/login"),
                "Ожидался редирект на /login, фактический Location: " + location);

        Assertions.assertNull(userRepository.findByLogin("unauth_user"),
                "Пользователь не должен быть сохранён без аутентификации");
    }
}
