package com.example.digitalplatform.db.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "student_info")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @OneToOne
    User user;
    String firstName;
    String LastName;
    String institute;
    double score;


}
