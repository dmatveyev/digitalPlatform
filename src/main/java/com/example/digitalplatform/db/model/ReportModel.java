package com.example.digitalplatform.db.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
public class ReportModel {

    private UUID userId;
    private String lastName;
    private String firstName;
    private String degree;
    private String institute;
    private SubjectArea subjectArea;
    private long countDone;
    private long countExpired;
    private long countDeclined;
    private long countAssigned;

}
