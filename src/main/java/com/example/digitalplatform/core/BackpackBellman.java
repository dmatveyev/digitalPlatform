package com.example.digitalplatform.core;


import com.example.digitalplatform.db.model.Request;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
@Service
public class BackpackBellman {

    public List<Request> executeByDP(Request[] requests, int n, int k) {
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

        return Arrays.stream(backpackWithMax.getRequests()).toList();
    }
}
