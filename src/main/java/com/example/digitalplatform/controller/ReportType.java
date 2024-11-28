package com.example.digitalplatform.controller;

public enum ReportType {

    BY_TEACHERS("Статистика завершенных работ по преподавателям","По преподавателям"),
    BY_SUBJECT_AREA("Статистика завершенных работ по предметным областям","По предметным областям"),
    BY_TEACHES_AND_SUBJECT_AREAS("Статистика завершенных работ по преподавателям в разрезе предметных областей","" +
            "По преподавателям и предметным областям");

    private final String desc;
    private final String shorgDesc;

    ReportType(String desc, String shorgDesc) {
        this.desc = desc;
        this.shorgDesc = shorgDesc;
    }

    public String getDesc() {
        return desc;
    }
    public String getShortDesc() {
        return shorgDesc;
    }
}
