package com.example.digitalplatform.service.handlers.rating;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import com.example.digitalplatform.db.repository.StudentInfoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AverageScoreParticipantRatingCalculator implements RatingCalculator {
    RatingParametersRepository ratingParametersRepository;
    StudentInfoRepository studentInfoRepository;

    @Override
    public void calculate(Request request) {
        RatingParameters ratingParameters = ratingParametersRepository.findByCode(getRatingName().name());
        if (Objects.nonNull(ratingParameters)) {
            double rating = request.getRating();
            WorkType workType = request.getWorkType();
            User customer = request.getCustomer();
            double score;
            StudentInfo byUser = studentInfoRepository.findByUser(customer);
            if (workType.equals(WorkType.INDIVIDUAL)) {
                score = byUser.getScore();
            } else {
                List<StudentInfo> byInstituteAndClazz = studentInfoRepository.getByInstituteAndClazz(byUser.getInstitute(), byUser.getClazz());
                DoubleSummaryStatistics doubleSummaryStatistics = byInstituteAndClazz.stream().mapToDouble(StudentInfo::getScore).summaryStatistics();
                score = doubleSummaryStatistics.getAverage();
            }
            double coefficient = ratingParameters.getCoefficient();
            rating += score * coefficient;
            request.setRating(rating);
        }
    }

    @Override
    public RatingName getRatingName() {
        return RatingName.AVERAGE_SCORE;
    }
}
