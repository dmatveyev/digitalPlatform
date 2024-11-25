package com.example.digitalplatform.service.handlers.userinfo;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.StudentInfoRepository;
import com.example.digitalplatform.dto.UserAccountDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentInfoService implements UserInfoService {

    StudentInfoRepository studentInfoRepository;

    @Override
    public UserAccountDto getUserInfo(User user) {
        StudentInfo info = studentInfoRepository.findByUser(user);
        UserAccountDto userAccountDto;
        if (Objects.nonNull(info)) {
            userAccountDto = fillUserAccountDto(user, info);
        } else {
            userAccountDto = fillUserAccountDto(user, new StudentInfo());
        }
        return userAccountDto;
    }

    @Override
    public void saveUserInfo(UserAccountDto dto, User user) {

        StudentInfo studentInfo = dto.getId() != null ? studentInfoRepository.findById(dto.getId()).orElse(new StudentInfo())
                : new StudentInfo();
        if (Objects.isNull(studentInfo.getUser())) {
            studentInfo.setUser(user);
        }
        studentInfo.setInstitute(dto.getInstitution());
        if (dto.getScore() != null) {
            studentInfo.setScore(dto.getScore());
        }
        if (dto.getClazz() != null) {
            studentInfo.setClazz(dto.getClazz());
        }
        studentInfoRepository.save(studentInfo);

    }
    @Override
    public RoleType getProcessingRole() {
        return RoleType.STUDENT;
    }


    private UserAccountDto fillUserAccountDto(User user, StudentInfo info) {
        UserAccountDto dto = new UserAccountDto();
        dto.setId(info.getId());
        dto.setUserName(user.getLogin());
        Role role = user.getRole();
        dto.setRoleCode(role.getCode());
        dto.setRoleName(role.getName());
        dto.setInstitution(info.getInstitute());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setScore(info.getScore());
        return dto;
    }
}
