package com.example.digitalplatform.core;


import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.TeacherInfo;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.MemPoolProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.openjdk.jmh.annotations.Mode.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@State(Scope.Benchmark)
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

    public List<Request> fullStoredTable(List<Request> list, TeacherInfo teacher) {
        int capacity = teacher.getLimitHours();
        int n = list.size();

        float[][] dp = new float[n + 1][capacity + 1];
        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= capacity; w++) {
                if (list.get(i - 1).getTime() <= w) {
                    dp[i][w] = Math.max(
                            dp[i - 1][w],
                            dp[i - 1][w - list.get(i - 1).getTime()] + list.get(i - 1).getRating() // Берем предмет
                    );
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }
        List<Request> requests = new ArrayList<>();
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


    public List<Request> optimizedBellman(List<Request> list, TeacherInfo teacher) {
        int capacity = teacher.getLimitHours();
        float[] dp1 = new float[capacity + 1];
        int[] backtrack = new int[capacity + 1];
        for (int i = 0; i < list.size(); i++) {
            Request item = list.get(i);
            for (int w = capacity; w >= item.getTime(); w--) {
                if (dp1[w] < dp1[w - item.getTime()] + item.getRating()) {
                    dp1[w] = dp1[w - item.getTime()] + item.getRating();
                    backtrack[w] = i;
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

    public BackpackBellman backpackBellman;
    public TeacherInfo info;
    public List<Request> requestList;


    @Setup(Level.Invocation)
    public void setUp() {
        backpackBellman = new BackpackBellman();
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
        requestList = List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
        int limitOurs = 20;
        info = new TeacherInfo();
        info.setLimitHours(limitOurs);
    }

    @Fork(value = 1)
    @Benchmark
    @BenchmarkMode({AverageTime, Throughput})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5)
    @Measurement(iterations = 10)
    public void fullStoredTable(Blackhole blackhole) {
        for (int i = 0; i < 1000; i++) {
            List<Request> requests = backpackBellman.fullStoredTable(requestList, info);
            blackhole.consume(requests);
        }
    }

    @Fork(value = 1)
    @Benchmark
    @BenchmarkMode({AverageTime, Throughput})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5)
    @Measurement(iterations = 10)
    public void memoryOptimized(Blackhole blackhole) {
        for (int i = 0; i < 1000; i++) {
            List<Request> requests = backpackBellman.optimizedBellman(requestList, info);
            blackhole.consume(requests);
        }

    }

    public static void main(String[] args) throws Exception {
        Main.main(args);
    }
}
