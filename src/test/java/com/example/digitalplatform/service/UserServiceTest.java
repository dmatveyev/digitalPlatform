package com.example.digitalplatform.service;

import com.example.digitalplatform.controller.dto.UserAccountDto;
import com.example.digitalplatform.db.model.Role;
import com.example.digitalplatform.db.model.RoleType;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.repository.RoleRepository;
import com.example.digitalplatform.db.repository.StudentInfoRepository;
import com.example.digitalplatform.db.repository.TeacherInfoRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты {@link UserService#registerNewUserAccount(UserAccountDto)} с моками репозиториев.
 *
 * Фиксируют поведение (контракт после Этапа 3.1 плана рефакторинга):
 * <ul>
 *   <li>дублирующий login &rarr; {@link org.springframework.web.server.ResponseStatusException}
 *       с кодом 409 (Conflict), а не тихий возврат существующего пользователя;</li>
 *   <li>null из {@code roleRepository.findByCode(...)} для неизвестной роли &rarr; явная ошибка
 *       ({@link org.springframework.web.server.ResponseStatusException} 400/404), а не NPE.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TeacherInfoRepository teacherInfoRepository;

    @Mock
    private StudentInfoRepository studentInfoRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * Экземпляр создаётся через конструктор вручную: {@code userInfoServices} передаём пустым списком —
     * registerNewUserAccount не использует serviceMap, поэтому маппинг сервисов в юнит-тестах не нужен.
     */
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, roleRepository,
                teacherInfoRepository, studentInfoRepository, List.of());
        // Инициируем приватный serviceMap (в production это делает @PostConstruct).
        userService.init();
    }

    private UserAccountDto account(String userName, String password, RoleType roleCode) {
        UserAccountDto dto = new UserAccountDto();
        dto.setUserName(userName);
        dto.setPassword(password);
        dto.setRoleCode(roleCode);
        return dto;
    }

    /**
     * Целевой контракт (Этап 3.1): существующий login &rarr; ResponseStatusException(409),
     * новый пользователь не создаётся, save() не вызывается.
     */
    @Test
    void registerNewUserAccountWithDuplicateLoginThrowsConflict() {
        when(userRepository.existsByLogin("dup_login")).thenReturn(true);

        org.springframework.web.server.ResponseStatusException ex = Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> userService.registerNewUserAccount(account("dup_login", "Secret#123", RoleType.STUDENT)));

        org.springframework.http.HttpStatusCode status = ex.getStatusCode();
        Assertions.assertTrue(status instanceof org.springframework.http.HttpStatus
                        && ((org.springframework.http.HttpStatus) status).value() == 409,
                "Дублирующий login должен давать 409 Conflict, фактически: " + status);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Целевой контракт (Этап 3.1): null из roleRepository.findByCode для неизвестной роли
     * &rarr; явная ошибка (ResponseStatusException), а не NPE / сохранение пользователя с null-ролью.
     */
    @Test
    void registerNewUserAccountWithUnknownRoleThrowsExplicitError() {
        when(userRepository.existsByLogin("new_user")).thenReturn(false);
        when(roleRepository.findByCode(RoleType.USER)).thenReturn(null);

        org.springframework.web.server.ResponseStatusException ex = Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> userService.registerNewUserAccount(account("new_user", "Secret#123", RoleType.USER)));

        // Контракт: 400 (невалидная роль из формы) или 404 (роль не найдена).
        org.springframework.http.HttpStatusCode status = ex.getStatusCode();
        Assertions.assertTrue(status instanceof org.springframework.http.HttpStatus
                        && (((org.springframework.http.HttpStatus) status).value() == 400
                            || ((org.springframework.http.HttpStatus) status).value() == 404),
                "Неизвестная роль должна давать 400/404, фактически: " + status);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Сигнатурный контракт (действует и до, и после рефакторинга): неизвестный login
     * и известная роль &rarr; пользователь сохраняется с закодированным паролем.
     */
    @Test
    void registerNewUserAccountWithValidDataSavesUser() {
        when(userRepository.existsByLogin("new_user")).thenReturn(false);

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode(RoleType.STUDENT);
        role.setName("Студент");
        when(roleRepository.findByCode(RoleType.STUDENT)).thenReturn(role);

        User toSave = new User();
        toSave.setLogin("new_user");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerNewUserAccount(account("new_user", "Secret#123", RoleType.STUDENT));

        Assertions.assertNotNull(saved, "Пользователь должен быть возвращён после сохранения");
        Assertions.assertEquals("new_user", saved.getLogin());
        Assertions.assertTrue(saved.isEnabled(), "Новый пользователь создаётся в активном состоянии");
        Assertions.assertSame(role, saved.getRole(), "Роль из формы должна быть установлена");

        verify(passwordEncoder).encode(eq("Secret#123"));
        Assertions.assertNotEquals("Secret#123", saved.getPassword(),
                "Пароль должен сохраняться закодированным, а не в открытом виде");
        verify(userRepository).save(any(User.class));
    }

}
