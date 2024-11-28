package com.example.digitalplatform.service.handlers.rating;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import com.example.digitalplatform.db.repository.StudentInfoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentInstituteRatingCalculator implements RatingCalculator {

    StudentInfoRepository studentInfoRepository;
    RatingParametersRepository ratingParametersRepository;

    @Override
    public void calculate(Request request) {
        double rating = request.getRating();
        User customer = request.getCustomer();
        RatingParameters byCode = ratingParametersRepository.findByCode(getRatingName().name());
        StudentInfo customerInfo = studentInfoRepository.findByUser(customer);
        if (Objects.nonNull(customerInfo)) {
            String institute = customerInfo.getInstitute().toLowerCase();
            if (institute.contains("школа") && !institute.contains("школа")) {
                rating += byCode.getMaxValue() * byCode.getCoefficient();
            } else if (institute.contains("техникум")) {
                rating += (byCode.getMaxValue() + byCode.getMinValue()) / 2 * byCode.getCoefficient();
            } else {
                rating += byCode.getMinValue() * byCode.getCoefficient();
            }
        } else {
            rating +=  (byCode.getMaxValue() + byCode.getMinValue()) / 2 * byCode.getCoefficient();
        }

        request.setRating(rating);
    }

    @Override
    public RatingName getRatingName() {
        return RatingName.TYPE_ORGANIZATION;
    }
}
