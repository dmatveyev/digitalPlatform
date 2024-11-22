package com.example.digitalplatform.dto;

import com.example.digitalplatform.db.model.RequestStatus;
import com.example.digitalplatform.db.model.SubjectArea;
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
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestDto {

    UUID id;
    String title;
    String description;
    SubjectArea subjectArea;
    int time;
    RequestStatus status;
    String creatorName;
    String assignedBy;
    LocalDateTime deadline;

}
