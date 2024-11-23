package com.example.digitalplatform.service;

import com.example.digitalplatform.core.BackpackBellman;
import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.RequestStatus;
import com.example.digitalplatform.db.model.TeacherInfo;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.repository.TeacherInfoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DecisionService {

    BackpackBellman backpackBellman;
    UserService userService;
    RequestService requestService;

    @Scheduled(cron = "0 */1 * * * *")
    public void assign() {
        List<TeacherInfo> teachers = userService.findTeacherInfos();
        List<Request> unassigned = requestService.findUnassigned();
        List<Request> processing = new ArrayList<>(unassigned);
        for (TeacherInfo teacherInfo : teachers) {
            List<Request> tempAssigned = backpackBellman.execute(processing, teacherInfo);
            processing.removeAll(tempAssigned);
            tempAssigned.forEach(request -> request.setWorker(teacherInfo.getUser()));
            tempAssigned.forEach(request -> request.setStatus(RequestStatus.ASSIGNED));
            requestService.updateList(tempAssigned);
        }
        if (!processing.isEmpty()) {
            processing.forEach(request -> request.setStatus(RequestStatus.NEW));
            requestService.updateList(processing);
        }
   }
}
