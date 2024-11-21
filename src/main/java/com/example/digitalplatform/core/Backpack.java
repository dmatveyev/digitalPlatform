package com.example.digitalplatform.core;

import com.example.digitalplatform.db.model.Request;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.stream.Collectors;

/**
 * Класс, описывающий состояние рюкзака
 */
@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Backpack {

    /**
     * Массив заявок в итерации
     */
    Request[] requests;
    /**
     * Подсчитанная стоимость занятий
     */
    double score;

    public String getDescription() {
        if (requests == null) {
            return "";
        } else {
            IntSummaryStatistics collect = Arrays.stream(requests).map(Request::getTime).collect(Collectors.summarizingInt(e -> e));
            return Arrays.stream(requests).map(Request::getTitle).collect(Collectors.joining("+")) + "-" + getScore() + "; "
                    + collect.getSum() + "h";
        }
    }
}
