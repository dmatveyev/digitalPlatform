package com.example.digitalplatform.db.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "teacher_info")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeacherInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @OneToOne
    User user;
    String institute;
    String degree;
    double score;
    @Column(name = "limit_hours")
    Integer limitHours;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "teachers_subject_areas",
            joinColumns = @JoinColumn(
                    name = "teacher_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "subjectarea_id", referencedColumnName = "id"))
    List<SubjectArea> subjectAreas;

}
