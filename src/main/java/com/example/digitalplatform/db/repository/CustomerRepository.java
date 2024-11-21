package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.Customer;
import com.example.digitalplatform.db.model.SubjectArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Customer findByUserId(UUID id);
}
