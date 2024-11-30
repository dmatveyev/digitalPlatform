package com.example.digitalplatform.core.dessision;

import com.example.digitalplatform.core.BackpackBellman;
import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.TeacherInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
public class BellmanTest {
    BackpackBellman backpackBellman = new BackpackBellman();
    @Test
    @DisplayName("Проверка алгоритма распределения запросов по принципу Беллмана")
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
        TeacherInfo teacherInfo = new TeacherInfo();
        teacherInfo.setLimitHours(limitOurs);
        List<Request> dpBellmanResult = backpackBellman.execute(requests, teacherInfo);
        List<Request> containsList = List.of(e1, e3, e5,e7,e9,e10);
        List<Request> notContainsList = List.of(e2,e4,e6,e8);
        assertTrue(dpBellmanResult.containsAll(containsList));
        for (Request request : notContainsList) {
            assertFalse(dpBellmanResult.contains(request));
        }
        int containsSumHours = dpBellmanResult.stream().mapToInt(Request::getTime).sum();
        assertEquals(limitOurs, containsSumHours);
    }
}
