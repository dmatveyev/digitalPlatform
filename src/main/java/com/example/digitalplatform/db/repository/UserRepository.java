package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.RoleType;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.service.UserService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByLogin(String login);

    List<User> findByRoleCodeAndAssignedNull(RoleType code);

    List<User> findByRoleCode(RoleType roleCode);
}
