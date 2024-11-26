package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.RequestStatus;
import com.example.digitalplatform.db.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.UUID;

public interface RequestRepository extends JpaRepository<Request, UUID>, PagingAndSortingRepository<Request, UUID> {
    List<Request> findByCustomer(User user);

    List<Request> findByWorker(User user);

    List<Request> findByStatus(RequestStatus requestStatus);

    Page<Request> findByCustomer(User user, Pageable pageable);

    Page<Request> findByWorker(User user, Pageable pageable);

    Page<Request> findByCustomerAndStatusIn(User user, Pageable pageable, List<RequestStatus> requestStatuses);

    Page<Request> findByWorkerAndStatusIn(User user, Pageable pageable, List<RequestStatus> requestStatuses);

    Page<Request> findByStatusIn(Pageable pageable, List<RequestStatus> requestStatuses);
}
