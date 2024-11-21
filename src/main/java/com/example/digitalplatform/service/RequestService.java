package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.Customer;
import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.SubjectArea;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.repository.CustomerRepository;
import com.example.digitalplatform.db.repository.RequestRepository;
import com.example.digitalplatform.db.repository.SubjectAreaRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.dto.RequestDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.example.digitalplatform.controller.Status.NEW;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestService {

    SubjectAreaRepository subjectAreaRepository;
    CustomerRepository customerRepository;
    UserRepository userRepository;
    RequestRepository requestRepository;
    RatingService ratingService;


    public void addRequest(RequestDto requestDto, String login) {

        Request request = new Request();
        request.setTitle(requestDto.getTitle());
        request.setDescription(requestDto.getDescription());
        request.setTime(requestDto.getTime());
        Optional<SubjectArea> byId = subjectAreaRepository.findById(requestDto.getSubjectAreaId());
        SubjectArea subjectArea = byId.orElseThrow();
        request.setSubjectArea(subjectArea);
        request.setStatus(NEW.name());
        User byLogin = userRepository.findByLogin(login);
        Customer customer = customerRepository.findByUserId(byLogin.getId());
        request.setCustomer(customer);
        ratingService.createRating(request);
        requestRepository.save(request);

    }

    public Request updateRequest(String id, RequestDto requestDto) {
        Request request = requestRepository.findById(UUID.fromString(id)).orElseThrow();
        request.setTime(requestDto.getTime());
        request.setDescription(requestDto.getDescription());
        return requestRepository.save(request);

    }
}
