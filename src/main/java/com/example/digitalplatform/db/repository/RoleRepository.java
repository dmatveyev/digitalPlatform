package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.Customer;
import com.example.digitalplatform.db.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByName(String name);
}
