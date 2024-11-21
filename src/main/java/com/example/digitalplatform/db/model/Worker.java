package com.example.digitalplatform.db.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workers")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    private User user;

    String firstName;
    String LastName;
    String institute;
    String degree;
    double score;
    @OneToMany(mappedBy = "worker")
    private List<Request> requests;
}
