package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.db.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RatingParametersRepository extends JpaRepository<RatingParameters, UUID> {
    RatingParameters findByCode(String name);
}
