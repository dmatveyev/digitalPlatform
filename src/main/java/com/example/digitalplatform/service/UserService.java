package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.Role;
import com.example.digitalplatform.db.model.RoleType;
import com.example.digitalplatform.db.model.TeacherInfo;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.repository.RoleRepository;
import com.example.digitalplatform.db.repository.StudentInfoRepository;
import com.example.digitalplatform.db.repository.TeacherInfoRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.controller.dto.RoleDto;
import com.example.digitalplatform.controller.dto.UserAccountDto;
import com.example.digitalplatform.service.handlers.userinfo.UserInfoService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис пользовательских аккаунтов: создание, пагинация, маппинг User → DTO.
 * Контракт статусов: 409 Conflict при дублирующем login, 404 Not Found при неизвестной роли.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final TeacherInfoRepository teacherInfoRepository;
    private final StudentInfoRepository studentInfoRepository;
    private final List<UserInfoService> userInfoServices;

    private Map<RoleType, UserInfoService> serviceMap;

    @PostConstruct
    public void init() {
        serviceMap = userInfoServices.stream().collect(Collectors.toMap(UserInfoService::getProcessingRole, Function.identity()));
    }


    /**
     * Создаёт новый пользовательский аккаунт.
     * - дублирующий login (запись с тем же login уже существует) -> 409 Conflict;
     * - неизвестная роль из формы (roleRepository.findByCode вернул null) -> 404 Not Found;
     * - успешное создание -> сохранённый пользователь.
     */
    public User registerNewUserAccount(UserAccountDto accountDto) {
        if (userRepository.existsByLogin(accountDto.getUserName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "There is an account with that user name: " + accountDto.getUserName());
        }

        Role byCode = roleRepository.findByCode(accountDto.getRoleCode());
        if (byCode == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown role code: '" + accountDto.getRoleCode() + "'");
        }

        User user = new User();
        user.setLogin(accountDto.getUserName());
        user.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        user.setEnabled(true);
        user.setRole(byCode);

        return userRepository.save(user);
    }


    /**
     * Возвращает страницу пользовательских аккаунтов (маппинг User → UserAccountDto через serviceMap).
     */
    public Page<UserAccountDto> getAllUserAccounts(Pageable pageable) {
        return mapUsersToDtosPage(userRepository.findAll(pageable));
    }

    /**
     * Возвращает список доступных ролей (code + name) для форм и фильтров.
     */
    public List<RoleDto> getAvailableRoles() {
        return roleRepository.findAll().stream()
                .map(role -> new RoleDto(role.getCode(), role.getName()))
                .toList();
    }

    /**
     * Возвращает преподавателей (роль TEACHER, без назначенных заявок).
     */
    public List<User> getTeachers() {
        List<User> teachers = userRepository.findByRoleCodeAndAssignedNull(RoleType.TEACHER);
        log.debug("Teachers count: {}", teachers.size());
        return teachers;
    }


    /**
     * Возвращает профили преподавателей (TeacherInfo) для всех пользователей с ролью TEACHER.
     */
    public List<TeacherInfo> findTeacherInfos() {
        return teacherInfoRepository.findByUserIn(getTeachers());
    }

    /**
     * Возвращает DTO пользователя по login (маппинг через UserInfoService для роли).
     */
    public UserAccountDto getUserInfoByUserName(String login) {
        User user = userRepository.findByLogin(login);
        return serviceMap.get(user.getRole().getCode()).getUserInfo(user);
    }

    /**
     * Обновляет базовые поля пользователя и специфичные для его роли (через UserInfoService).
     */
    public void saveUserInfo(UserAccountDto dto, User user) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setMiddleName(dto.getMiddleName());
        userRepository.save(user);
        serviceMap.get(user.getRole().getCode()).saveUserInfo(dto, user);
    }

    /**
     * Возвращает страницу пользовательских аккаунтов с заданной ролью (маппинг User → UserAccountDto через serviceMap).
     */
    public Page<UserAccountDto> findByRoleCode(RoleType roleCode, Pageable pageable) {
        return mapUsersToDtosPage(userRepository.findByRoleCode(roleCode, pageable));
    }

    private List<UserAccountDto> mapUsersToDtos(List<User> users) {
        return users.stream()
                .map(user -> serviceMap.get(user.getRole().getCode()).getUserInfo(user))
                .toList();
    }

    private Page<UserAccountDto> mapUsersToDtosPage(Page<User> users) {
        List<UserAccountDto> dtos = mapUsersToDtos(users.getContent());
        return new PageImpl<>(dtos, users.getPageable(), users.getTotalElements());
    }
}
