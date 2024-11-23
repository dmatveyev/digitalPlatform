package com.example.digitalplatform.service;

import com.example.digitalplatform.core.GeneratorDessisions;
import com.example.digitalplatform.db.model.*;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DecisionService {

    UserService userService;
    RequestService requestService;
    List<GeneratorDessisions> generators;
    @NonFinal
    Map<String, GeneratorDessisions> generatorsMap;
    @NonFinal
    @Value("${generators.type}")
    String generatorType;


    @PostConstruct
    public void init() {
        generatorsMap = generators.stream().collect(Collectors.toMap(GeneratorDessisions::generatorType, Function.identity()));
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void automaticAssign() {
        log.debug("Process assigning request was start automatically");
        List<Request> processing = getRequests();
        log.debug("Automatic process assigning was end. Unassigned request count:{}", processing.size());
   }

    public void manualAssign(UUID userId) {
        log.debug("Process assigning request was start by user with id: {}", userId);
        List<Request> processing = getRequests();
        log.debug("Process assigning was end. Unassigned request count:{}", processing.size());
    }

    private List<Request> getRequests() {
        GeneratorDessisions generatorDessisions = generatorsMap.get(generatorType);
        if (Objects.isNull(generatorDessisions)) {
            generatorDessisions = generatorsMap.get("BACKPACK_BELLMAN");
        }
        List<TeacherInfo> teachers = userService.findTeacherInfos();
        List<Request> unassigned = requestService.findUnassigned();
        List<Request> processing = new ArrayList<>(unassigned);
        log.debug("Available request count: {}", processing.size());
        for (TeacherInfo teacherInfo : teachers) {
            List<Request> available = getAvailableRequests(teacherInfo, processing);
            List<Request> tempAssigned = generatorDessisions.execute(available, teacherInfo);
            processing.removeAll(tempAssigned);
            tempAssigned.forEach(request -> request.setWorker(teacherInfo.getUser()));
            tempAssigned.forEach(request -> request.setStatus(RequestStatus.ASSIGNED));
            requestService.updateList(tempAssigned);
        }
        if (!processing.isEmpty()) {
            processing.forEach(request -> request.setStatus(RequestStatus.NEW));
            requestService.updateList(processing);
        }
        return processing;
    }

    private List<Request> getAvailableRequests(TeacherInfo teacherInfo, List<Request> processing) {
        Map<UUID, List<Request>> requestBySubjectArea = processing.stream().collect(Collectors.groupingBy
                (request -> request.getSubjectArea().getId()));
        List<SubjectArea> subjectAreas = teacherInfo.getSubjectAreas();
        List<Request> result = new ArrayList<>();
        for (SubjectArea subjectArea : subjectAreas) {
            List<Request> requests = requestBySubjectArea.getOrDefault(subjectArea.getId(), Collections.emptyList());
            result.addAll(requests);
        }
        return result;
    }
}
