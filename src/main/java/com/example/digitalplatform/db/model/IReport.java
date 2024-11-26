package com.example.digitalplatform.db.model;

public interface IReport {

    String getUserId();

    String getLastName();

    String getFirstName();

    String getDegree();

    String getInstitute();

    String getSubjectArea();

    String getRequestStatus();

    long getCountDone();

    long getCountExpired();
}
