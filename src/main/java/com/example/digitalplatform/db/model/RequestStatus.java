package com.example.digitalplatform.db.model;

public enum RequestStatus {
    NEW("Новая"),
    PLANNING("В планировании"),
    ASSIGNED("Назначена"),
    PROCESSED("В обработке"),
    FINISHED("Завершена");

    private final String desc;
    RequestStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc()
    {
        return this.desc;
    }
}
