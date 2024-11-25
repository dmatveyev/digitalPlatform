package com.example.digitalplatform.service.handlers.rating;

import com.example.digitalplatform.db.model.RatingName;
import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PeriodicalWorkRatingCalculator implements RatingCalculator {

    RatingParametersRepository ratingParametersRepository;
    @Override
    public void calculate(Request request) {
        RatingParameters byCode = ratingParametersRepository.findByCode(getRatingName().name());
        double rating = request.getRating();
        double value = request.isPeriodical() ? byCode.getMaxValue(): byCode.getMinValue();
        rating += value * byCode.getCoefficient();
        request.setRating(rating);
    }

    @Override
    public RatingName getRatingName() {
        return RatingName.PERIODICAL_WORK;
    }
}
