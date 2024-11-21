package com.example.digitalplatform.db.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Objects;
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
    Customer customer;
    @ManyToOne
    Worker worker;
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

    String status;

}
