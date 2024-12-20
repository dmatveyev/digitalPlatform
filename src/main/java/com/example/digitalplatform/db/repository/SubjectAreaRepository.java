package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.SubjectArea;
import com.example.digitalplatform.db.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubjectAreaRepository extends JpaRepository<SubjectArea, UUID> {

    SubjectArea findByName(String name);
}
