package com.example.digitalplatform.dto;

public enum UserType {
    TEACHER("Преподаватель"),
    STUDENT("Учащийся");

    private final String name;

    UserType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
