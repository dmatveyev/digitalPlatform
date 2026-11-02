package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.RoleRepository;
import com.example.digitalplatform.db.repository.StudentInfoRepository;
import com.example.digitalplatform.db.repository.TeacherInfoRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.controller.dto.RoleDto;
import com.example.digitalplatform.controller.dto.UserAccountDto;
import com.example.digitalplatform.service.handlers.userinfo.UserInfoService;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    TeacherInfoRepository teacherInfoRepository;
    StudentInfoRepository studentInfoRepository;

    List<UserInfoService> userInfoServices;

    @NonFinal
    Map<RoleType, UserInfoService> serviceMap;

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
        User old = userRepository.findByLogin(accountDto.getUserName());
        if (Objects.nonNull(old)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "There is an account with that user name: " + accountDto.getUserName());
        }
        User user = new User();
        user.setLogin(accountDto.getUserName());
        user.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        user.setEnabled(true);

        Role byCode = roleRepository.findByCode(accountDto.getRoleCode());
        if (Objects.isNull(byCode)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown role code: '" + accountDto.getRoleCode() + "'");
        }
        user.setRole(byCode);
        User saved = userRepository.save(user);

        return saved;
    }


    public List<UserAccountDto> getAllUserAccounts() {
        List<User> users = userRepository.findAll();
        return mapUsersToDtos(users);
    }


    public List<RoleDto> getAvailableRoles() {
        List<Role> all = roleRepository.findAll();
        List<RoleDto> dtos = all.stream().map(role ->
                new RoleDto(role.getCode(), role.getName())
        ).toList();
        return dtos;
    }

    public List<User> getTeachers() {
        List<User> byRoleCodeAndAssignedNull = userRepository.findByRoleCodeAndAssignedNull(RoleType.TEACHER);
        log.debug("Teachers count: {}", byRoleCodeAndAssignedNull.size());
        return byRoleCodeAndAssignedNull;
    }


    public List<TeacherInfo> findTeacherInfos() {
        List<User> teachers = getTeachers();
        return teacherInfoRepository.findByUserIn(teachers);
    }

    public UserAccountDto getUserInfoByUserName(String login) {
        User user = userRepository.findByLogin(login);
        Role role = user.getRole();
        UserInfoService userInfoService = serviceMap.get(role.getCode());
        UserAccountDto userInfo = userInfoService.getUserInfo(user);
        return userInfo;

    }

    public void saveUserInfo(UserAccountDto dto, User user) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setMiddleName(dto.getMiddleName());
        userRepository.save(user);
        UserInfoService userInfoService = serviceMap.get(user.getRole().getCode());
        userInfoService.saveUserInfo(dto, user);
    }

    public List<UserAccountDto> findByRoleCode(RoleType roleCode) {
        List<User> users = userRepository.findByRoleCode(roleCode);
        return mapUsersToDtos(users);
    }

    private List<UserAccountDto> mapUsersToDtos(List<User> users) {
        return users.stream()
                .map(user -> serviceMap.get(user.getRole().getCode()).getUserInfo(user))
                .toList();
    }
}
