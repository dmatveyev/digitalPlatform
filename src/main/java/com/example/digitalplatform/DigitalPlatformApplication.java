package com.example.digitalplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

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
        String str = "Пахомова Василиса Святославовна; Львов Георгий Сергеевич; Копылова Ева Александровна; Максимова Юлия Павловна; Воронин Андрей Артёмович; Спиридонова Анна Михайловна; Лазарев Максим Константинович; Греков Фёдор Александрович; Романова Виктория Дмитриевна; Морозова Екатерина Алексеевна; Панова Виктория Львовна; Панова София Данииловна; Никитин Владимир Никитич; Иванова Нина Михайловна; Захарова Мирослава Николаевна; Николаев Иван Степанович; Парфенов Тимофей Ильич; Осипова Валерия Тимофеевна; Герасимов Матвей Артёмович; Громов Мирон Дмитриевич; Карасева Елизавета Андреевна; Борисов Андрей Семёнович; Блинова Ксения Витальевна; Кононов Павел Матвеевич; Лебедева Вероника Матвеевна; Петухова Алиса Марковна; Белова Эвелина Святославовна; Фролова Мария Ивановна; Воронцова Стефания Всеволодовна; Уткина Злата Александровна; Голованов Родион Алиевич; Миронова Ксения Павловна; Гусева Анна Владимировна; Степанова Алиса Валерьевна; Быков Пётр Александрович; Никонов Григорий Михайлович; Васильева Вероника Тихоновна; Балашова Марта Фёдоровна; Журавлев Ярослав Даниилович; Матвеев Дмитрий Маркович";
        List<String> names = Arrays.stream(str.split("; ")).toList();
        List<String[]> strings = names.stream().map(s -> s.split(" ")).toList();
        return strings;
    }

    @Bean
    public List<String[]> teachersNames() {
        String str = "Петрова Виктория Васильевна; Белякова Ксения Олеговна; Макаров Максим Глебович; Мартынов Михаил Михайлович; Дьяконов Михаил Георгиевич; Афанасьева Светлана Саввична; Беляков Сергей Русланович; Воронов Григорий Тимурович; Николаев Илья Георгиевич; Михайлов Максим Андреевич";
        List<String> names = Arrays.stream(str.split("; ")).toList();
        List<String[]> strings = names.stream().map(s -> s.split(" ")).toList();
        return strings;
    }

    @Bean
    public List<String> subjectNames() {
        String subjects = """
                Русский и родной язык;
                Математика;
                литература;
                Английский язык;
                Китайский язык;
                История;
                Обществознание;
                Физика;
                Химия;
                Биология;
                География;
                """;
        List<String> subjectNames = Arrays.stream(subjects.split(";\n")).toList();
        return subjectNames;
    }

}
