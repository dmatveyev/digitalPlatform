package com.example.digitalplatform;

import com.example.digitalplatform.controller.dto.CreateRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class DigitalPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(DigitalPlatformApplication.class, args);
    }

    @Bean
    public List<String[]> studentsNames() {
        String str = "Пахомова Василиса Святославовна; Львов Георгий Сергеевич; Копылова Ева Александровна; Максимова Юлия Павловна; Воронин Андрей Артёмович; Спиридонова Анна Михайловна; Лазарев Максим Константинович; Греков Фёдор Александрович; Романова Виктория Дмитриевна; Морозова Екатерина Алексеевна; Панова Виктория Львовна; Панова София Данииловна; Никитин Владимир Никитич; Иванова Нина Михайловна; Захарова Мирослава Николаевна; Николаев Иван Степанович; Парфенов Тимофей Ильич; Осипова Валерия Тимофеевна; Герасимов Матвей Артёмович; Громов Мирон Дмитриевич; Карасева Елизавета Андреевна; Борисов Андрей Семёнович";
        List<String> names = Arrays.stream(str.split("; ")).toList();
        List<String[]> strings = names.stream().map(s -> s.split(" ")).toList();
        return strings;
    }

    @Bean
    public List<String[]> teachersNames() {
        String str = "Петрова Виктория Васильевна; Белякова Ксения Олеговна; Макаров Максим Глебович; Мартынов Михаил Михайлович; Дьяконов Михаил Георгиевич; Афанасьева Светлана Саввична; ";
        List<String> names = Arrays.stream(str.split("; ")).toList();
        List<String[]> strings = names.stream().map(s -> s.split(" ")).toList();
        return strings;
    }

    @Bean
    public List<String> subjectNames() {
        String subjects = """
                Русский язык;
                Математика;
                Английский язык;
                Физика;
                """;
        List<String> subjectNames = Arrays.stream(subjects.split(";\n")).toList();
        return subjectNames;
    }


    @Bean
    @SneakyThrows
    public List<Subject> subjects() {
        ObjectMapper mapper = new ObjectMapper();
        File file = ResourceUtils.getFile("classpath:subjects.json");
        Root root = mapper.readValue(file, Root.class);
        List<Subject> subjects = root.getSubjects();
        // Далее работайте с данными
        return subjects;
    }

}
