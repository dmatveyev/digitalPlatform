package com.example.digitalplatform.controller.dto;

import com.example.digitalplatform.db.model.RoleType;
import com.example.digitalplatform.db.model.SubjectArea;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAccountDto {

    UUID id;

    @NotBlank(message = "userName не может быть пустым")
    @Size(min = 3, max = 50, message = "userName: длина от 3 до 50 символов")
    String userName;

    @NotBlank(message = "password не может быть пустым")
    @Size(min = 8, max = 100, message = "password: длина от 8 до 100 символов")
    String password;
    String firstName;
    String lastName;
    String middleName;
    String institution;
    String clazz;

    @NotNull(message = "roleCode не может быть null")
    RoleType roleCode;
    String roleName;
    String degree;
    Integer limitHours;
    Float score;
    List<SubjectArea> subjectAreas;

}
