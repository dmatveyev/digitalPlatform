package com.example.digitalplatform;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    public static void main(String[] args) {
        LocalDate parse = LocalDate.parse("2024-12-12", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        parse.atStartOfDay();
    }
}
