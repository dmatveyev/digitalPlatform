package com.example.digitalplatform.db.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Упрощенная модель заявки на проведение занятия с преподавателем по выбранной теме
 */
@Entity
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    /**
     * Наименование заявки
     */
    String title;
    String description;
    @ManyToOne
    User customer;
    @ManyToOne
    User worker;
    @ManyToOne()
    SubjectArea subjectArea;
    /**
     * Количество часов занятия
     */
    int time;
    /**
     * Стоимость проведения занятия в баллах
     */
    double rating;
    Integer participantsScore;
    Integer teacherScore;

    @Enumerated(EnumType.STRING)
    RequestStatus status;
    @Column(name = "work_type")
    @Enumerated(EnumType.STRING)
    WorkType workType;
    boolean periodical;

    @Column(name = "creation_date")
    LocalDateTime creationDate;
    @Column(name = "planed_finish_date")
    LocalDateTime planedFinishDate;
    @Column(name = "actual_finish_date")
    LocalDateTime actualFinishDate;

    public Request(String title, int time, int rating) {
        this.title = title;
        this.time = time;
        this.rating = rating;
    }

}
