package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.Role;
import com.example.digitalplatform.db.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByCode(RoleType name);

    Role findByName(String roleName);
}
