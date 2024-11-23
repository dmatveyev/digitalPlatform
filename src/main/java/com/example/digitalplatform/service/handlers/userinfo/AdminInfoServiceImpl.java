package com.example.digitalplatform.service.handlers.userinfo;

import com.example.digitalplatform.db.model.RoleType;
import org.springframework.stereotype.Service;

@Service
public class AdminInfoServiceImpl implements UserInfoService {

    @Override
    public RoleType getProcessingRole() {
        return RoleType.ADMIN;
    }
}
