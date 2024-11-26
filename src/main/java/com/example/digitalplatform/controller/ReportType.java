package com.example.digitalplatform.controller;

public enum ReportType {

    BY_TEACHERS("По преподавателям"),
    BY_SUBJECT_AREA("По предметным областям"),
    BY_TEACHES_AND_SUBJECT_AREAS("По преподавателям и предметным областям");

    private final String desc;

    ReportType(String s) {
        this.desc = s;
    }

    public String getDesc() {
        return desc;
    }
}
