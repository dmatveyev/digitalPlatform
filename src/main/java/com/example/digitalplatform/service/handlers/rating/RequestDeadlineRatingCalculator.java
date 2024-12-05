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

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestDeadlineRatingCalculator implements RatingCalculator {

    RatingParametersRepository ratingParametersRepository;

    @Override
    public void calculate(Request request) {
        RatingParameters byCode = ratingParametersRepository.findByCode(getRatingName().name());
    float rating = request.getRating();
        LocalDateTime planedFinishDate = request.getPlanedFinishDate();
        LocalDateTime now = LocalDateTime.now();
        if (now.plusDays(7).isAfter(planedFinishDate))  {
            rating += byCode.getMaxValue() * byCode.getCoefficient();
        } else if (now.plusDays(14).isAfter(planedFinishDate)) {
            rating += (byCode.getMaxValue() + byCode.getMinValue()) / 2 * byCode.getCoefficient();
        } else {
            rating += byCode.getMinValue() * byCode.getCoefficient();
        }
        request.setRating(rating);
    }

    @Override
    public RatingName getRatingName() {
        return RatingName.DEADLINE;
    }
}
