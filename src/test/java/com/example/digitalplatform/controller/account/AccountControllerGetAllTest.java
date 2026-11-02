package com.example.digitalplatform.controller.account;

import com.example.digitalplatform.controller.dto.UserAccountDto;
import com.example.digitalplatform.db.model.Role;
import com.example.digitalplatform.db.model.RoleType;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.repository.RoleRepository;
import com.example.digitalplatform.db.repository.UserRepository;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.example.digitalplatform.DigitalPlatformApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerGetAllTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * data.sql загружает seed-пользователя с фиксированным UUID.
     */
    private static final UUID SEED_USER_ID = UUID.fromString("aa25d9af-f18b-4bf1-bef6-f70176b89b52");

    /**
     * Роли загружаются из БД один раз на весь класс (они неизменны в тестах),
     * чтобы createUser не ходил в базу за ролью при каждом вызове.
     */
    private Map<RoleType, Role> rolesByCode;

    @BeforeEach
    void setUp() {
        // Очищаем таблицу users (включая seed из data.sql), чтобы каждый тест начинался с пустой таблицы.
        userRepository.deleteAll();
        if (rolesByCode == null) {
            rolesByCode = roleRepository.findAll().stream()
                    .collect(Collectors.toMap(Role::getCode, Function.identity()));
        }
    }

    private User createUser(String login, RoleType roleCode) {
        User user = new User();
        user.setLogin(login);
        // Не хешируем пароль: для маппинга DTO в тестах значение пароля не используется.
        user.setPassword("plain-password");
        user.setEnabled(true);
        user.setRole(rolesByCode.get(roleCode));
        return user;
    }

    /**
     * T1: пустая таблица users -> 200, атрибуты accounts/roles/roleCode присутствуют,
     * totalElements == 0, content пуст.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllWithEmptyUsersTableReturnsEmptyPage() throws Exception {
        MvcResult result = mockMvc.perform(get("/accounts/all"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("accounts"))
                .andExpect(model().attributeExists("roles"))
                .andReturn();

        Page<UserAccountDto> accounts = (Page<UserAccountDto>) result.getModelAndView().getModelMap().get("accounts");
        Assertions.assertEquals(0, accounts.getTotalElements(), "Пустая таблица users -> totalElements == 0");
        Assertions.assertTrue(accounts.getContent().isEmpty(), "Пустая таблица users -> content пуст");

        List<RoleType> roles = (List<RoleType>) result.getModelAndView().getModelMap().get("roles");
        Assertions.assertNotNull(roles, "Атрибут 'roles' должен присутствовать");
    }

    /**
     * T2: 3 пользователя вперемешку -> totalElements == 3, все на странице по умолчанию (size=5),
     * порядок соответствует порядку репозитория.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllReturnsAllUsersOnDefaultPage() throws Exception {
        userRepository.save(createUser("charlie", RoleType.STUDENT));
        userRepository.save(createUser("alice", RoleType.TEACHER));
        userRepository.save(createUser("bravo", RoleType.USER));

        MvcResult result = mockMvc.perform(get("/accounts/all"))
                .andExpect(status().isOk())
                .andReturn();

        Page<UserAccountDto> accounts = (Page<UserAccountDto>) result.getModelAndView().getModelMap().get("accounts");
        Assertions.assertEquals(3, accounts.getTotalElements(), "totalElements должен равняться 3");
        Assertions.assertEquals(3, accounts.getContent().size(), "Страница по умолчанию должна содержать все 3 записи");

        List<String> logins = accounts.getContent().stream().map(UserAccountDto::getUserName).collect(Collectors.toList());
        // Порядок определяется репозиторием (findAll без явной сортировки) — фиксируем текущее поведение.
        Assertions.assertEquals(List.of("charlie", "alice", "bravo"), logins,
                "Порядок пользователей должен совпадать с порядком репозитория");
    }

    /**
     * T3: 7 пользователей, ?page=1&size=5 -> totalElements == 7, content.size() == 5 (первые по порядку).
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllPaginationFirstPage() throws Exception {
        for (int i = 1; i <= 7; i++) {
            userRepository.save(createUser("user_" + i, RoleType.USER));
        }

        MvcResult result = mockMvc.perform(get("/accounts/all").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andReturn();

        Page<UserAccountDto> accounts = (Page<UserAccountDto>) result.getModelAndView().getModelMap().get("accounts");
        Assertions.assertEquals(7, accounts.getTotalElements(), "totalElements == 7");
        Assertions.assertEquals(5, accounts.getContent().size(), "Первая страница size=5 содержит 5 записей");

        List<String> logins = accounts.getContent().stream().map(UserAccountDto::getUserName).collect(Collectors.toList());
        Assertions.assertEquals(List.of("user_1", "user_2", "user_3", "user_4", "user_5"), logins,
                "Первая страница должна содержать первые 5 пользователей по порядку репозитория");
    }

    /**
     * T4: 7 пользователей, ?page=2&size=5 -> totalElements == 7, content.size() == 2 (последние).
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllPaginationLastPagePartial() throws Exception {
        for (int i = 1; i <= 7; i++) {
            userRepository.save(createUser("user_" + i, RoleType.USER));
        }

        MvcResult result = mockMvc.perform(get("/accounts/all").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andReturn();

        Page<UserAccountDto> accounts = (Page<UserAccountDto>) result.getModelAndView().getModelMap().get("accounts");
        Assertions.assertEquals(7, accounts.getTotalElements(), "totalElements == 7");
        Assertions.assertEquals(2, accounts.getContent().size(), "Последняя страница содержит оставшиеся 2 записи");

        List<String> logins = accounts.getContent().stream().map(UserAccountDto::getUserName).collect(Collectors.toList());
        Assertions.assertEquals(List.of("user_6", "user_7"), logins,
                "Вторая страница должна содержать последние 2 пользователя по порядку репозитория");
    }

    /**
     * T5: ?role=TEACHER возвращает только пользователей с ролью TEACHER.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllRoleFilterReturnsOnlyUsersOfThatRole() throws Exception {
        userRepository.save(createUser("teacher_1", RoleType.TEACHER));
        userRepository.save(createUser("student_1", RoleType.STUDENT));
        userRepository.save(createUser("teacher_2", RoleType.TEACHER));
        userRepository.save(createUser("user_1", RoleType.USER));

        MvcResult result = mockMvc.perform(get("/accounts/all").param("role", "TEACHER"))
                .andExpect(status().isOk())
                .andReturn();

        Page<UserAccountDto> accounts = (Page<UserAccountDto>) result.getModelAndView().getModelMap().get("accounts");
        Assertions.assertEquals(2, accounts.getTotalElements(), "Фильтр по роли TEACHER -> totalElements == 2");
        List<String> logins = accounts.getContent().stream().map(UserAccountDto::getUserName).collect(Collectors.toList());
        Assertions.assertEquals(List.of("teacher_1", "teacher_2"), logins,
                "В результате должны быть только пользователи с ролью TEACHER");

        Object roleCodeAttr = result.getModelAndView().getModelMap().get("roleCode");
        Assertions.assertEquals(RoleType.TEACHER, roleCodeAttr, "Атрибут 'roleCode' должен содержать выбранную роль");
    }

    /**
     * T6: комбинация фильтра по роли и пагинации.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllRoleFilterCombinedWithPagination() throws Exception {
        for (int i = 1; i <= 7; i++) {
            userRepository.save(createUser("teacher_" + i, RoleType.TEACHER));
        }
        userRepository.save(createUser("student_1", RoleType.STUDENT));

        MvcResult result = mockMvc.perform(get("/accounts/all")
                .param("page", "2").param("size", "5").param("role", "TEACHER"))
                .andExpect(status().isOk())
                .andReturn();

        Page<UserAccountDto> accounts = (Page<UserAccountDto>) result.getModelAndView().getModelMap().get("accounts");
        Assertions.assertEquals(7, accounts.getTotalElements(), "totalElements учитывает только отфильтрованных по роли");
        Assertions.assertEquals(2, accounts.getContent().size(), "Вторая страница size=5 содержит 2 записи");

        List<String> logins = accounts.getContent().stream().map(UserAccountDto::getUserName).collect(Collectors.toList());
        Assertions.assertEquals(List.of("teacher_6", "teacher_7"), logins,
                "Страница должна содержать только пользователей с ролью TEACHER");
    }

    /**
     * T7: аутентифицированный пользователь без роли ADMIN -> 403.
     */
    @Test
    @WithMockUser(username = "user", authorities = {"USER"})
    void getAllWithoutAdminRoleReturns403() throws Exception {
        mockMvc.perform(get("/accounts/all"))
                .andExpect(status().isForbidden());
    }

    /**
     * T8: НЕАВТОРИЗОВАННЫЙ запрос (без @WithMockUser) -> 3xx редирект на /login.
     */
    @Test
    void getAllWithoutAuthenticationRedirectsToLogin() throws Exception {
        MvcResult result = mockMvc.perform(get("/accounts/all"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        Assertions.assertNotNull(location, "Неавторизованный GET /accounts/all должен вернуть 3xx с Location");
        Assertions.assertTrue(location.contains("/login"),
                "Ожидался редирект на /login, фактический Location: " + location);
    }
}
