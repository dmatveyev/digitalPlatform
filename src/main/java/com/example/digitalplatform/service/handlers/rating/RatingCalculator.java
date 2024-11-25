package com.example.digitalplatform.service.handlers.rating;

import com.example.digitalplatform.db.model.RatingName;
import com.example.digitalplatform.db.model.Request;

public interface RatingCalculator {

   void calculate(Request request);

    RatingName getRatingName();
}
