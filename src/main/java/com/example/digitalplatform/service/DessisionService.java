package com.example.digitalplatform.service;

import com.example.digitalplatform.core.BackpackBellman;
import com.example.digitalplatform.db.model.Request;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DessisionService {

    BackpackBellman backpackBellman;

    void dess() {
       /* Request[] requests = {
                new Request("Заявка 1", 1, 8),
                new Request("Заявка 2", 4, 2),
                new Request("Заявка 3", 2, 5),
                new Request("Заявка 4", 3, 3),
                new Request("Заявка 5", 4, 7),
                new Request("Заявка 6", 8, 9),
                new Request("Заявка 7", 6, 8),
                new Request("Заявка 8", 4, 4),
                new Request("Заявка 9", 4, 7),
                new Request("Заявка 10", 3, 5)
        };
        int n = requests.length;
        int k = 20;
        List<Request> requestList = new ArrayList<>();
        Collections.addAll(requestList, requests);
        System.out.println("Bellman");
        List<Request> dpBellmanResult = backpackBellman.executeByDP(requests, n, k);
        System.out.println(dpBellmanResult);
        System.out.println("Total time: " + dpBellmanResult.stream().mapToInt(Request::getTime).sum() + ", total score: "+ dpBellmanResult.stream().mapToInt(Request::getRating).sum());
   */ }
}
