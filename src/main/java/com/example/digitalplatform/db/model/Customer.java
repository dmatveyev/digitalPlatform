package com.example.digitalplatform.db.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    private User user;

    String firstName;
    String LastName;
    String school;
    double score;

    @OneToMany(mappedBy = "customer")
    private List<Request> requests;

}
