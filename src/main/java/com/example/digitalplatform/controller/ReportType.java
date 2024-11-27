package com.example.digitalplatform.controller;

public enum ReportType {

    BY_TEACHERS("Статистика завершенных работ по преподавателям"),
    BY_SUBJECT_AREA("Статистика завершенных работ по предметным областям"),
    BY_TEACHES_AND_SUBJECT_AREAS("Статистика завершенных работ по преподавателям в разрезе предметных областей");

    private final String desc;

    ReportType(String s) {
        this.desc = s;
    }

    public String getDesc() {
        return desc;
    }
}
