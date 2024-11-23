package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.StudentInfoRepository;
import com.example.digitalplatform.db.repository.TeacherInfoRepository;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDetailInfoService {

    TeacherInfoRepository teacherInfoRepository;
    StudentInfoRepository studentInfoRepository;


    public String getUserName(User user) {
        Role role = user.getRole();
        RoleType code = role.getCode();
        String name = switch (code) {
            case STUDENT -> {
                StudentInfo byUser = studentInfoRepository.findByUser(user);
                if (Objects.nonNull(byUser)) {
                    yield byUser.getFirstName().concat(" ").concat(byUser.getLastName());
                } else {
                    yield "";
                }
            }
            case TEACHER -> {
                TeacherInfo byUser = teacherInfoRepository.findByUser(user);
                yield byUser.getDegree().concat(" ").concat(byUser.getFirstName().concat(" ").concat(byUser.getLastName()));
            }
            default -> user.getLogin();
        };
        return name;
    }
}
