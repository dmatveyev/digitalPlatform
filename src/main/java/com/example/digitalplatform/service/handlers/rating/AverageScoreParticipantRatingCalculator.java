package com.example.digitalplatform.service.handlers.rating;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import com.example.digitalplatform.db.repository.StudentInfoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.aspectj.apache.bcel.classfile.Deprecated;
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
        float rating = request.getRating();
            WorkType workType = request.getWorkType();
            User customer = request.getCustomer();
        float score;
            StudentInfo byUser = studentInfoRepository.findByUser(customer);
            if (Objects.nonNull(byUser)) {
                if (workType.equals(WorkType.INDIVIDUAL)) {
                    score = byUser.getScore();
                } else {
                    List<StudentInfo> byInstituteAndClazz = studentInfoRepository.getByInstituteAndClazz(byUser.getInstitute(), byUser.getClazz());
                    DoubleSummaryStatistics doubleSummaryStatistics = byInstituteAndClazz.stream().mapToDouble(StudentInfo::getScore).summaryStatistics();
                    score = (float) doubleSummaryStatistics.getAverage();
                }
            float coefficient = ratingParameters.getCoefficient();
                rating += score * coefficient;
            } else {
                rating += (ratingParameters.getMinValue() + ratingParameters.getMaxValue()) / 2 * ratingParameters.getCoefficient();
            }
            request.setRating(rating);
        }
    }

    @Override
    public RatingName getRatingName() {
        return RatingName.AVERAGE_SCORE;
    }
}
