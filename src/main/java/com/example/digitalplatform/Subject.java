package com.example.digitalplatform;

import lombok.Data;

import java.util.List;

@Data
public class Subject {
    private String name;
    private List<Topic> topics;

    // Геттеры и сеттеры
}