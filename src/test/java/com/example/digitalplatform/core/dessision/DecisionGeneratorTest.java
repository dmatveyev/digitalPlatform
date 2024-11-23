package com.example.digitalplatform.core.dessision;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.service.DecisionService;
import com.example.digitalplatform.service.RequestService;
import com.example.digitalplatform.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DecisionGeneratorConfig.class)
public class DecisionGeneratorTest {

    public static final String MATH = "Math";
    public static final UUID MATH_ID = UUID.randomUUID();
    @Autowired
    DecisionService decisionService;
    @Autowired
    UserService userService;
    @Autowired
    RequestService requestService;
    @Captor
    ArgumentCaptor<List<Request>> requestsCaptor;



    @Test
    void assignForOne() {
        List<Request> requests = getRequests();
        User user = getUser();
        SubjectArea area = getSubjectArea(MATH);
        TeacherInfo info1 = getTeacherInfo(user, List.of(area));
        when(userService.findTeacherInfos()).thenReturn(List.of(info1));
        when(requestService.findUnassigned()).thenReturn(requests);
        doNothing().when(requestService).updateList(requestsCaptor.capture());
        decisionService.assign();
        List<List<Request>> allValues = requestsCaptor.getAllValues();
        assertEquals(2, allValues.size());
        List<Request> assigned = allValues.get(0);
        boolean isActualStatus = assigned.stream().allMatch(request -> request.getStatus().equals(RequestStatus.ASSIGNED));
        boolean isWorkerAssigned = assigned.stream().allMatch(request -> request.getWorker().getId().equals(user.getId()));
        assertTrue(isActualStatus);
        assertTrue(isWorkerAssigned);
        List<Request> nonAssigned = allValues.get(1);
        isActualStatus = nonAssigned.stream().allMatch(request -> request.getStatus().equals(RequestStatus.NEW));
        isWorkerAssigned = nonAssigned.stream().allMatch(request -> request.getWorker() == null);
        assertTrue(isActualStatus);
        assertTrue(isWorkerAssigned);
    }

    private SubjectArea getSubjectArea(String name) {
        if (name.equals(MATH)) {
            return new SubjectArea(MATH_ID, name, "");

        } else {
            return new SubjectArea(UUID.randomUUID(), name, "");
        }
    }

    private static TeacherInfo getTeacherInfo(User user, List<SubjectArea> areas) {
        TeacherInfo info1 = new TeacherInfo();
        info1.setUser(user);
        info1.setLimitHours(20);
        info1.setSubjectAreas(areas);
        return info1;
    }

    @Test
    void assignAllRequest() {
        List<Request> requests = getRequests();
        User user = getUser();
        User user2 = getUser();
        TeacherInfo info1 = getTeacherInfo(user, List.of(getSubjectArea(MATH)));
        TeacherInfo info2 = getTeacherInfo(user2, List.of(getSubjectArea(MATH)));
        when(userService.findTeacherInfos()).thenReturn(List.of(info1, info2));
        when(requestService.findUnassigned()).thenReturn(requests);
        doNothing().when(requestService).updateList(requestsCaptor.capture());
        decisionService.assign();
        List<List<Request>> allValues = requestsCaptor.getAllValues();
        assertEquals(2, allValues.size());
        List<Request> assignedFirst = allValues.get(0);
        boolean isActualStatus = assignedFirst.stream().allMatch(request -> request.getStatus().equals(RequestStatus.ASSIGNED));
        boolean isWorkerAssigned = assignedFirst.stream().allMatch(request -> request.getWorker().getId().equals(user.getId()));
        assertTrue(isActualStatus);
        assertTrue(isWorkerAssigned);
        List<Request> nonAssignedSecond = allValues.get(1);
        isActualStatus = nonAssignedSecond.stream().allMatch(request -> request.getStatus().equals(RequestStatus.ASSIGNED));
        isWorkerAssigned = nonAssignedSecond.stream().allMatch(request -> request.getWorker().getId().equals(user2.getId()));
        assertTrue(isActualStatus);
        assertTrue(isWorkerAssigned);
    }
    private User getUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }
    private List<Request> getRequests() {
        Request e1 = new Request("Заявка 1", 1, 8);
        e1.setSubjectArea(getSubjectArea(MATH));
        Request e2 = new Request("Заявка 2", 4, 2);
        e2.setSubjectArea(getSubjectArea(MATH));
        Request e3 = new Request("Заявка 3", 2, 5);
        e3.setSubjectArea(getSubjectArea(MATH));
        Request e4 = new Request("Заявка 4", 3, 3);
        e4.setSubjectArea(getSubjectArea(MATH));
        Request e5 = new Request("Заявка 5", 4, 7);
        e5.setSubjectArea(getSubjectArea(MATH));
        Request e6 = new Request("Заявка 6", 8, 9);
        e6.setSubjectArea(getSubjectArea(MATH));
        Request e7 = new Request("Заявка 7", 6, 8);
        e7.setSubjectArea(getSubjectArea(MATH));
        Request e8 = new Request("Заявка 8", 4, 4);
        e8.setSubjectArea(getSubjectArea(MATH));
        Request e9 = new Request("Заявка 9", 4, 7);
        e9.setSubjectArea(getSubjectArea(MATH));
        Request e10 = new Request("Заявка 10", 3, 5);
        e10.setSubjectArea(getSubjectArea(MATH));
        List<Request> requests = List.of( e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
        return requests;
    }
}
