package com.example.digitalplatform.service.handlers.rating;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import com.example.digitalplatform.db.repository.RequestRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthorRatingCalculator implements RatingCalculator {

    RequestRepository requestRepository;
    RatingParametersRepository ratingParametersRepository;
    @Override
    public void calculate(Request request) {

        User customer = request.getCustomer();
        List<Request> requestList = requestRepository.findByCustomerAndStatusIn(customer, List.of(RequestStatus.DECLINED,
                RequestStatus.FINISHED));
        RatingParameters byCode = ratingParametersRepository.findByCode(getRatingName().name());
        double defaultRating = (byCode.getMaxValue() - byCode.getMinValue()) / 2 * byCode.getCoefficient();
        double rating = request.getRating();
        if (requestList.isEmpty()) {
            rating += defaultRating;
            request.setRating(rating);
        } else {
            Map<RequestStatus, Long> countMap = requestList.stream().collect(Collectors.groupingBy(Request::getStatus,
                    Collectors.counting()));
            Long finishedCount = countMap.getOrDefault(RequestStatus.FINISHED, 0L);
            Long declinedCount = countMap.getOrDefault(RequestStatus.DECLINED, 0L);

            if (2 * finishedCount > declinedCount) {
                rating += byCode.getMaxValue() * byCode.getCoefficient();
            } else if (2* declinedCount > finishedCount) {
                rating += byCode.getMinValue() * byCode.getCoefficient();
            } else {
                rating += defaultRating;
            }
        }
        request.setRating(rating);
    }

    @Override
    public RatingName getRatingName() {
        return RatingName.AUTHOR_PREVIOUS_REQUESTS;
    }
}
