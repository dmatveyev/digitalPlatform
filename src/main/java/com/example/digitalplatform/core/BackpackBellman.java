package com.example.digitalplatform.core;


import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.TeacherInfo;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class BackpackBellman implements GeneratorDecision {

    @Override
    public List<Request> execute(List<Request> list, TeacherInfo teacher) {
        log.debug("Starting creating optimal request list for teacher with id: {}", teacher.getId());
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Request> requests = fullStoredTable(list, teacher);
        log.debug("Optimal requests list was created. For teacher with id: {} request count is: {}",
                teacher.getId(), requests.size());
        return requests;
    }

    private List<Request> fullStoredTable(List<Request> list, TeacherInfo teacher) {
        int capacity = teacher.getLimitHours();
        int n = list.size();

        double[][] dp = new double[n + 1][capacity + 1];
        // Заполнение таблицы DP
        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= capacity; w++) {
                if (list.get(i - 1).getTime() <= w) {
                    dp[i][w] = Math.max(
                            dp[i - 1][w], // Не берем предмет
                            dp[i - 1][w - list.get(i - 1).getTime()] + list.get(i - 1).getRating() // Берем предмет
                    );
                } else {
                    dp[i][w] = dp[i - 1][w]; // Не берем предмет
                }
            }
        }
        List<Request> requests = new ArrayList<>();
        // Извлечение списка предметов из таблицы DP
        int w = capacity;
        for (int i = n; i > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                Request item = list.get(i - 1);
                requests.add(item);
                w -= item.getTime();
            }
        }
        return requests;
    }


    private List<Request> optimizedBellman(List<Request> list, TeacherInfo teacher) {
        int capacity = teacher.getLimitHours();
        int n = list.size();


        double[] dp1 = new double[capacity + 1];
        int[] backtrack = new int[capacity + 1];
        for (int i = 0; i < list.size(); i++) {
            Request item = list.get(i);
            for (int w = capacity; w >= item.getTime(); w--) {
                if (dp1[w] < dp1[w - item.getTime()] + item.getRating()) {
                    dp1[w] = dp1[w - item.getTime()] + item.getRating();
                    backtrack[w] = i; // Сохраняем индекс последнего добавленного предмета
                }
            }
        }
        List<Request> selectedItems = new ArrayList<>();
        int w = capacity;
        while (w > 0 && backtrack[w] != -1) {
            int itemIndex = backtrack[w];
            selectedItems.add(list.get(itemIndex));
            w -= list.get(itemIndex).getTime();
        }
        return selectedItems;
    }


    @Override
    public GeneratorType generatorType() {
        return GeneratorType.BACKPACK_BELLMAN;
    }
}
