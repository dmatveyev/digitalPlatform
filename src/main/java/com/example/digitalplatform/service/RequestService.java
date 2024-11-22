package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.RequestRepository;
import com.example.digitalplatform.db.repository.SubjectAreaRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.dto.CreateRequestDto;
import com.example.digitalplatform.dto.RequestDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

import static com.example.digitalplatform.controller.Status.NEW;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestService {

    SubjectAreaRepository subjectAreaRepository;
    UserRepository userRepository;
    RequestRepository requestRepository;
    RatingService ratingService;


    public void addRequest(CreateRequestDto createRequestDto, String login) {

        Request request = new Request();
        request.setTitle(createRequestDto.getTitle());
        request.setDescription(createRequestDto.getDescription());
        request.setTime(createRequestDto.getTime());
        Optional<SubjectArea> byId = subjectAreaRepository.findById(createRequestDto.getSubjectAreaId());
        SubjectArea subjectArea = byId.orElseThrow();
        request.setSubjectArea(subjectArea);
        request.setStatus(NEW.name());
        User byLogin = userRepository.findByLogin(login);
        request.setCustomer(byLogin);
        ratingService.createRating(request);
        requestRepository.save(request);

    }

    public Request updateRequest(String id, CreateRequestDto createRequestDto) {
        Request request = requestRepository.findById(UUID.fromString(id)).orElseThrow();
        request.setTime(createRequestDto.getTime());
        request.setDescription(createRequestDto.getDescription());
        return requestRepository.save(request);

    }

    public List<RequestDto> findByPrincipal(Principal principal) {
        User user = userRepository.findByLogin(principal.getName());
        Role role = user.getRole();
        RoleType code = role.getCode();
        List<Request> requests = switch (code) {
            case STUDENT -> requestRepository.findByCustomer(user);
            case TEACHER -> requestRepository.findByWorker(user);
            case ADMIN -> requestRepository.findAll();
            case USER -> Collections.emptyList();
        };
        List<RequestDto> result = new ArrayList<>();
        for (Request request : requests) {
            RequestDto dto = getRequestDto(request);
            result.add(dto);
        }

        return result;
    }

    private RequestDto getRequestDto(Request request) {
        RequestDto dto = new RequestDto();
        dto.setId(request.getId());
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setStatus(request.getStatus());
        dto.setTime(request.getTime());
        dto.setSubjectArea(request.getSubjectArea());
        User customer = request.getCustomer();
        dto.setCreatorName(customer.getFirstName() + " " + customer.getLastName());
        User worker = request.getWorker();
        if (Objects.nonNull(worker)) {
            dto.setAssignedBy(worker.getDegree() + " " + worker.getFirstName() + " " + worker.getLastName());
        }
        return dto;
    }

    public RequestDto findById(UUID uuid) {
        Request request = requestRepository.findById(uuid).orElseThrow();
        return getRequestDto(request);
    }
}
