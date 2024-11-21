package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequestRepository extends JpaRepository<Request, UUID> {
}
