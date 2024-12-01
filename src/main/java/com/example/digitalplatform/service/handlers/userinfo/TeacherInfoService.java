package com.example.digitalplatform.service.handlers.userinfo;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.TeacherInfoRepository;
import com.example.digitalplatform.controller.dto.UserAccountDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherInfoService implements UserInfoService {

    TeacherInfoRepository teacherInfoRepository;

    @Override
    public UserAccountDto getUserInfo(User user) {
        TeacherInfo info = teacherInfoRepository.findByUser(user);
        UserAccountDto userAccountDto;
        if (Objects.nonNull(info)) {
            userAccountDto = fillUserAccountDto(user, info);
        } else {
            userAccountDto = fillUserAccountDto(user, new TeacherInfo());
        }
        return userAccountDto;
    }

    @Override
    public RoleType getProcessingRole() {
        return RoleType.TEACHER;
    }

    @Override
    public void saveUserInfo(UserAccountDto dto, User user) {

        TeacherInfo teacherInfo = dto.getId() != null ?  teacherInfoRepository.findById(dto.getId()).orElse(new TeacherInfo())
                : new TeacherInfo();
        if (Objects.isNull(teacherInfo.getUser())) {
            teacherInfo.setUser(user);
        }
        teacherInfo.setInstitute(dto.getInstitution());
        teacherInfo.setDegree(dto.getDegree());
        teacherInfo.setLimitHours(dto.getLimitHours());
        teacherInfo.setSubjectAreas(dto.getSubjectAreas());
        teacherInfoRepository.save(teacherInfo);
    }


    private UserAccountDto fillUserAccountDto(User user, TeacherInfo info) {
        UserAccountDto dto = new UserAccountDto();
        dto.setId(info.getId());
        dto.setUserName(user.getLogin());
        Role role = user.getRole();
        dto.setRoleCode(role.getCode());
        dto.setRoleName(role.getName());
        dto.setInstitution(info.getInstitute());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setDegree(info.getDegree());
        dto.setLimitHours(info.getLimitHours());
        dto.setSubjectAreas(info.getSubjectAreas());
        return dto;
    }
}
