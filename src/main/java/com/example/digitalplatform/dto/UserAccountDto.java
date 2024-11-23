package com.example.digitalplatform.dto;

import com.example.digitalplatform.db.model.Role;
import com.example.digitalplatform.db.model.RoleType;
import com.example.digitalplatform.db.model.SubjectArea;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAccountDto {

    UUID id;
    String userName;
    String password;
    String firstName;
    String lastName;
    String institution;
    RoleType roleCode;
    String roleName;
    String degree;
    Integer limitHours;
    Double score;
    List<SubjectArea> subjectAreas;

}
