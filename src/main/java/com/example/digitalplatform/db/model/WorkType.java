package com.example.digitalplatform.db.model;

public enum WorkType {
    INDIVIDUAL("Индивидуальное"),
    GROUP("Групповое")
    ;

    private final String desc;

    WorkType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }
}
