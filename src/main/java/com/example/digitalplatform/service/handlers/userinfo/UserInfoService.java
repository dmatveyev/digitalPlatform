package com.example.digitalplatform.service.handlers.userinfo;

import com.example.digitalplatform.db.model.RoleType;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.controller.dto.UserAccountDto;

public interface UserInfoService {

    default UserAccountDto getUserInfo(User user) {
        UserAccountDto dto = new UserAccountDto();
        dto.setUserName(user.getLogin());
        dto.setRoleName(user.getRole().getName());
        dto.setRoleCode(getProcessingRole());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        return dto;
    }

    RoleType getProcessingRole();

    default void saveUserInfo(UserAccountDto userAccountDto, User user){};
}
