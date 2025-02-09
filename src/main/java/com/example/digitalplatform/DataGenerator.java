package com.example.digitalplatform;

import com.example.digitalplatform.db.model.RoleType;
import com.example.digitalplatform.db.model.SubjectArea;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.model.WorkType;
import com.example.digitalplatform.db.repository.SubjectAreaRepository;
import com.example.digitalplatform.controller.dto.CreateRequestDto;
import com.example.digitalplatform.controller.dto.UserAccountDto;
import com.example.digitalplatform.service.RequestService;
import com.example.digitalplatform.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataGenerator {

    UserService userService;
    RequestService requestService;
    SubjectAreaRepository subjectAreaRepository;
    List<String[]> teachersNames;
    List<String[]> studentsNames;
    List<String> subjectNames;
    List<Subject> subjects;

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        createSubjectAreas();

        List<SubjectArea> subjectAreas = subjectAreaRepository.findAll();
        Map<String, SubjectArea> areasMap = subjectAreas.stream().collect(Collectors.toMap(SubjectArea::getName, Function.identity()));
        for (int i = 1; i < teachersNames.size() + 1; i++) {
            UserAccountDto teacherAccountDto = getTeacherAccountDto(subjectAreas, i);
            User user = userService.registerNewUserAccount(teacherAccountDto);
            userService.saveUserInfo(teacherAccountDto, user);

        }
        List<String> students = new ArrayList<>();
        for (int i = 1; i < studentsNames.size() + 1; i++) {
            UserAccountDto studentAccountDto = getStudentAccountDto(i);
            User student = userService.registerNewUserAccount(studentAccountDto);
            userService.saveUserInfo(studentAccountDto, student);
            students.add(student.getLogin());
        }

        for (Subject subject : subjects) {
            List<Topic> topics = subject.getTopics();
            SubjectArea area = areasMap.get(subject.getName());
            for (Topic topic : topics) {
                int randomValue = ThreadLocalRandom.current().nextInt(0, students.size() - 1);
                CreateRequestDto createRequestDto = getCreateRequestDto(area, topic);
                requestService.addRequest(createRequestDto, students.get(randomValue));
            }
        }

    }

    private void createSubjectAreas() {
        for (String subjectName : subjectNames) {
            SubjectArea subjectArea = new SubjectArea();
            subjectArea.setId(UUID.randomUUID());
            subjectArea.setName(subjectName);
            subjectAreaRepository.save(subjectArea);
        }

    }

    private UserAccountDto getTeacherAccountDto(List<SubjectArea> subjectAreas, int i) {
        UserAccountDto dto = new UserAccountDto();
        dto.setSubjectAreas(getSubjectAreas(subjectAreas));
        dto.setUserName("teacher" + i);
        int index = ThreadLocalRandom.current().nextInt(0, teachersNames.size() - 1);
        dto.setFirstName(teachersNames.get(index)[1]);
        dto.setLastName(teachersNames.get(index)[0]);
        dto.setMiddleName(teachersNames.get(index)[2]);
        dto.setPassword("teacher");
        dto.setInstitution(getTeacherInstitution());
        dto.setRoleCode(RoleType.TEACHER);
        dto.setDegree(randomDegree());
        dto.setLimitHours(getLimitHours(5, 20));
        dto.setScore(getScore(1, 10));
        return dto;
    }

    private UserAccountDto getStudentAccountDto(int i) {
        UserAccountDto dto = new UserAccountDto();
        dto.setUserName("student_" + i);
        int index = ThreadLocalRandom.current().nextInt(0, studentsNames.size() - 1);
        dto.setFirstName(studentsNames.get(index)[1]);
        dto.setLastName(studentsNames.get(index)[0]);
        dto.setMiddleName(studentsNames.get(index)[2]);
        dto.setPassword("student");
        dto.setInstitution(getStudentInstitution());
        dto.setRoleCode(RoleType.STUDENT);
        dto.setClazz(String.valueOf(ThreadLocalRandom.current().nextInt(1, 11)));
        dto.setScore(getScore(1, 5));
        return dto;
    }

    private List<SubjectArea> getSubjectAreas(List<SubjectArea> subjectAreas) {
        int countAreas = ThreadLocalRandom.current().nextInt(1, subjectAreas.size());
        List<SubjectArea> shuffle = new ArrayList<>(subjectAreas);
        Collections.shuffle(shuffle);
        List<SubjectArea> result = new ArrayList<>();
        for (int i = 0; i < countAreas; i++) {
            result.add(subjectAreas.get(i));
        }
        return result;
    }

    private String getTeacherInstitution() {
        int randomValue = ThreadLocalRandom.current().nextInt(1, 3);
        return switch (randomValue) {
            case 1 -> "ПГУТИ";
            case 2 -> "СамГТУ";
            case 3 -> "Самарский университет";
            default -> "";
        };
    }

    private String getStudentInstitution() {
        int randomValue = ThreadLocalRandom.current().nextInt(1, 5);
        return switch (randomValue) {
            case 1 -> "Школа 1";
            case 2 -> "Школа 2";
            case 3 -> "Техникум 99";
            default -> "Вечерняя школа";
        };
    }

    private String randomDegree() {
        int randomValue = ThreadLocalRandom.current().nextInt(1, 3);
        return switch (randomValue) {
            case 1 -> "Профессор";
            case 2 -> "Доцент";
            case 3 -> "Аспирант";
            default -> "";
        };

    }

    private CreateRequestDto getCreateRequestDto(SubjectArea subject, Topic topic) {
        CreateRequestDto createRequestDto = new CreateRequestDto();
        createRequestDto.setTitle(topic.getTitle());
        createRequestDto.setDescription(topic.getDescription());
        createRequestDto.setSubjectAreaId(subject.getId());
        createRequestDto.setWorkType(new Random().nextBoolean() ? WorkType.INDIVIDUAL : WorkType.GROUP);
        createRequestDto.setPeriodical(new Random().nextBoolean());
        long days = ThreadLocalRandom.current().nextLong(1, 30);
        createRequestDto.setDeadline(LocalDateTime.now().plusDays(days));
        createRequestDto.setTime(ThreadLocalRandom.current().nextInt(1, 10));
        return createRequestDto;
    }

    private float getScore(int rangeMin, int rangeMax) {
        Random r = new Random();
        float randomValue = (float) ThreadLocalRandom.current().nextDouble(rangeMin, rangeMax);
        return randomValue;
    }

    private Integer getLimitHours(int rangeMin, int rangeMax) {
        Random r = new Random();
        Integer randomValue = ThreadLocalRandom.current().nextInt(rangeMin, rangeMax);
        return randomValue;
    }
}
