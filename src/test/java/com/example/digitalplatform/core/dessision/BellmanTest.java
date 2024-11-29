package com.example.digitalplatform.core.dessision;

import com.example.digitalplatform.core.BackpackBellman;
import com.example.digitalplatform.core.BranchAndBound;
import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.RequestStatus;
import com.example.digitalplatform.db.model.TeacherInfo;
import com.example.digitalplatform.db.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class BellmanTest {

    BackpackBellman backpackBellman = new BackpackBellman();
    BranchAndBound branchAndBound = new BranchAndBound();

    @Test
    void success() {
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
        int limitOurs = 20;
        User user = new User();
        user.setId(UUID.randomUUID());
        TeacherInfo teacherInfo = new TeacherInfo();
        teacherInfo.setLimitHours(limitOurs);
        teacherInfo.setUser(user);
        System.out.println("Bellman");
        List<Request> dpBellmanResult = backpackBellman.execute(requests, teacherInfo);
        List<Request> bbResult = branchAndBound.execute(requests, teacherInfo);
        assertTrue(dpBellmanResult.contains(e1) && bbResult.contains(e1));
        assertTrue(dpBellmanResult.contains(e3) && bbResult.contains(e3));
        assertTrue(dpBellmanResult.contains(e5) && bbResult.contains(e5));
        assertTrue(dpBellmanResult.contains(e7) && bbResult.contains(e7));
        assertTrue(dpBellmanResult.contains(e9) && bbResult.contains(e9));
        assertTrue(dpBellmanResult.contains(e10) && bbResult.contains(e10));
        int sum = dpBellmanResult.stream().mapToInt(Request::getTime).sum();
        int sum1 = bbResult.stream().mapToInt(Request::getTime).sum();
        assertEquals(limitOurs, sum);
        assertEquals(limitOurs, sum1);
    }
}
