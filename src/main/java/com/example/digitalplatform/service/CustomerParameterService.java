package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.RatingName;
import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerParameterService implements RatingParameterService {
    RatingParametersRepository ratingParametersRepository;

    @Override
    public void calculate(Request request) {
        RatingParameters ratingParameters = ratingParametersRepository.findByCode(getRatingName().name());
        if (Objects.nonNull(ratingParameters)) {
            double rating = request.getRating();
            User customer = request.getCustomer();
            double score = 5d;
            double coefficient = ratingParameters.getCoefficient();
            rating += score * coefficient;
            request.setRating(rating);
        }
    }

    @Override
    public RatingName getRatingName() {
        return RatingName.AUTHOR_RATING;
    }
}
