package com.example.digitalplatform.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

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

}
