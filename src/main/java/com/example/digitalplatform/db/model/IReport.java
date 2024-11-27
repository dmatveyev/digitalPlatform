package com.example.digitalplatform.db.model;

public interface IReport {

    String getUserId();

    String getLastName();

    String getFirstName();

    String getDegree();

    String getInstitute();

    String getSubjectArea();

    long getCountDone();
    long getCountDeclined();
    long getCountAssigned();

    long getCountExpired();
}
