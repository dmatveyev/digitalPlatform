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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
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


    public User registerNewUserAccount(UserAccountDto accountDto) {
        User old = userRepository.findByLogin(accountDto.getUserName());
        if (Objects.nonNull(old)) {
            log.error("There is an account with that user name: " + accountDto.getUserName());
            return old;
        }
        User user = new User();
        user.setLogin(accountDto.getUserName());
        user.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        user.setEnabled(true);

        Role byCode = roleRepository.findByCode(accountDto.getRoleCode());
        user.setRole(byCode);
        User saved = userRepository.save(user);

        return saved;
    }


    public List<UserAccountDto> getAllUserAccounts() {
        List<User> users = userRepository.findAll();
        List<UserAccountDto> result = new ArrayList<>();
        for (User user : users) {
            UserInfoService userInfoService = serviceMap.get(user.getRole().getCode());
            UserAccountDto userInfo = userInfoService.getUserInfo(user);
            result.add(userInfo);
        }
        return result;
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
        userRepository.save(user);
        UserInfoService userInfoService = serviceMap.get(user.getRole().getCode());
        userInfoService.saveUserInfo(dto, user);
    }

    public List<UserAccountDto> findByRoleCode(RoleType roleCode) {
        List<User> users = userRepository.findByRoleCode(roleCode);
        List<UserAccountDto> result = new ArrayList<>();
        for (User user : users) {
            UserInfoService userInfoService = serviceMap.get(user.getRole().getCode());
            UserAccountDto userInfo = userInfoService.getUserInfo(user);
            result.add(userInfo);
        }
        return result;
    }
}
