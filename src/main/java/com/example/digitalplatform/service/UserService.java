package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.RoleRepository;
import com.example.digitalplatform.db.repository.StudentInfoRepository;
import com.example.digitalplatform.db.repository.TeacherInfoRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.dto.RoleDto;
import com.example.digitalplatform.dto.UserAccountDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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


    public User registerNewUserAccount(UserAccountDto accountDto)  {
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
        List<TeacherInfo> teacherInfos = teacherInfoRepository.findByUserIn(users);
        Map<User, TeacherInfo> teacherMap = teacherInfos.stream().collect(Collectors.toMap(TeacherInfo::getUser, Function.identity()));
        List<StudentInfo> studentInfos = studentInfoRepository.findByUserIn(users);
        Map<User, StudentInfo> studentMap = studentInfos.stream().collect(Collectors.toMap(StudentInfo::getUser, Function.identity()));
        for (User user : users) {
            if (teacherMap.containsKey(user)) {
                TeacherInfo teacher = teacherMap.get(user);
                fillUserAccountDto(result, user, teacher.getInstitute(), teacher.getFirstName(), teacher.getLastName());
            } else if (studentMap.containsKey(user)) {
                StudentInfo studentInfo = studentMap.get(user);
                fillUserAccountDto(result, user, studentInfo.getInstitute(), studentInfo.getFirstName(), studentInfo.getLastName());
            } else {
                fillUserAccountDto(result, user, "", "", "");
            }
        }
        return result;
    }

    private void fillUserAccountDto(List<UserAccountDto> result, User user, String institute, String firstName, String lastName) {
        UserAccountDto dto = new UserAccountDto();
        dto.setUserName(user.getLogin());
        Role role = user.getRole();
        dto.setRoleCode(role.getCode());
        dto.setRoleName(role.getName());
        dto.setInstitution(institute);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        result.add(dto);
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
}
