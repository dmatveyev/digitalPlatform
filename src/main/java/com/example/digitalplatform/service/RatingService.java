package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.service.handlers.rating.RatingParameterService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RatingService {

    List<RatingParameterService> parameterServiceList;

    void createRating(Request request) {
        parameterServiceList.forEach(service -> service.calculate(request));
    }
}
