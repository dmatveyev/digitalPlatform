package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.RoleType;
import com.example.digitalplatform.db.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByLogin(String login);

    boolean existsByLogin(String login);

    List<User> findByRoleCodeAndAssignedNull(RoleType code);

    List<User> findByRoleCode(RoleType roleCode);

    Page<User> findByRoleCode(RoleType roleCode, Pageable pageable);
}
