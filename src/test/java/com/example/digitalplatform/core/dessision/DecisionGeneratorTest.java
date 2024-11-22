package com.example.digitalplatform.core.dessision;

import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.RequestStatus;
import com.example.digitalplatform.db.model.User;
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
        when(userService.getTeachers()).thenReturn(List.of(user));
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

    @Test
    void assignAllRequest() {
        List<Request> requests = getRequests();
        User user = getUser();
        User user2 = getUser();
        when(userService.getTeachers()).thenReturn(List.of(user, user2));
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
        int limitOurs = 20;
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setLimitOurs(limitOurs);
        return user;
    }
    private List<Request> getRequests() {
        Request e1 = new Request("Заявка 1", 1, 8);
        Request e2 = new Request("Заявка 2", 4, 2);
        Request e3 = new Request("Заявка 3", 2, 5);
        Request e4 = new Request("Заявка 4", 3, 3);
        Request e5 = new Request("Заявка 5", 4, 7);
        Request e6 = new Request("Заявка 6", 8, 9);
        Request e7 = new Request("Заявка 7", 6, 8);
        Request e8 = new Request("Заявка 8", 4, 4);
        Request e9 = new Request("Заявка 9", 4, 7);
        Request e10 = new Request("Заявка 10", 3, 5);
        List<Request> requests = List.of( e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
        return requests;
    }
}
