package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.Privilege;
import com.example.digitalplatform.db.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrivilegeRepository extends JpaRepository<Privilege, UUID> {
    Privilege findByName(String name);
}
