package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.RatingName;
import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.db.model.Request;

public interface RatingParameterService {

   void calculate(Request request);

    RatingName getRatingName();
}
