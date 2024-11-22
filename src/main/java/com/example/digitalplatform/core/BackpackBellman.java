package com.example.digitalplatform.core;


import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.User;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class BackpackBellman implements GeneratorDessisions {

    @Override
    public List<Request> execute(List<Request> list, User user) {
        log.debug("Начинаем формирование оптимального списка заявок для пользователя с id {}", user.getId());
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        int k = user.getLimitOurs();
        int n = list.size();
        Request[] requests = list.toArray(Request[]::new);
        Backpack[][] bp = new Backpack[n + 1][k + 1];
        for (int i = 0; i < n + 1; i++) {
            for (int j = 0; j < k + 1; j++) {
                if (i == 0 || j == 0) {
                    bp[i][j] = new Backpack(new Request[]{}, 0);
                } else if (i == 1) {
                    bp[1][j] = requests[0].getTime() <= j ? new Backpack(new Request[]{requests[0]}, requests[0].getRating())
                            : new Backpack(new Request[]{}, 0);
                } else {
                    if (requests[i - 1].getTime() > j)
                        bp[i][j] = bp[i - 1][j];
                    else {
                        Backpack[] backpacks = bp[i - 1];
                        double newScore = requests[i - 1].getRating() + backpacks[j - requests[i - 1].getTime()].getScore();
                        if (bp[i - 1][j].getScore() > newScore)
                            bp[i][j] = bp[i - 1][j];
                        else {
                            bp[i][j] = new Backpack(Stream.concat(Arrays.stream(new Request[]{requests[i - 1]}),
                                    Arrays.stream(bp[i - 1][j - requests[i - 1].getTime()].getRequests()))
                                    .toArray(Request[]::new), newScore);
                        }
                    }
                }
            }
        }
        List<Backpack> lastColumn = Arrays.stream(bp).map(row -> row[row.length - 1]).toList();
        Backpack backpackWithMax = lastColumn.stream().max(Comparator.comparing(Backpack::getScore))
                .orElse(new Backpack(null, 0));
        Request[] requestArr = backpackWithMax.getRequests();
        log.debug("Формирование списка завершено. Для пользователя с id: {} количество подходящих заявок: {}",
                user.getId(), requestArr.length);
        return Arrays.stream(requestArr).toList();
    }

    public String generatorType() {
        return "BACKPACK_BELLMAN";
    }
}
