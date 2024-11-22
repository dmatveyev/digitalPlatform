package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.RequestStatus;
import com.example.digitalplatform.db.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RequestRepository extends JpaRepository<Request, UUID> {
    List<Request> findByCustomer(User user);

    List<Request> findByWorker(User user);

    List<Request> findByStatus(RequestStatus requestStatus);
}
