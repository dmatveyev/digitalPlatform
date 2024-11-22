package com.example.digitalplatform.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Упрощенная модель заявки на проведение занятия с преподавателем по выбранной теме
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateRequestDto {

    String title;
    String description;
    UUID subjectAreaId;
    int time;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")
    LocalDateTime deadline;

}
