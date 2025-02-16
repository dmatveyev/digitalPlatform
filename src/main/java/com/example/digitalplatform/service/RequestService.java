package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.RequestRepository;
import com.example.digitalplatform.db.repository.SubjectAreaRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.controller.dto.CreateRequestDto;
import com.example.digitalplatform.controller.dto.RequestDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestService {

    SubjectAreaRepository subjectAreaRepository;
    UserRepository userRepository;
    RequestRepository requestRepository;
    RatingService ratingService;
    UserDetailInfoService userDetailInfoService;


    public void addRequest(CreateRequestDto createRequestDto, String login) {

        Request request = new Request();
        request.setTitle(createRequestDto.getTitle());
        request.setDescription(createRequestDto.getDescription());
        request.setTime(createRequestDto.getTime());
        Optional<SubjectArea> byId = subjectAreaRepository.findById(createRequestDto.getSubjectAreaId());
        SubjectArea subjectArea = byId.orElseThrow();
        request.setSubjectArea(subjectArea);
        request.setStatus(RequestStatus.NEW);
        request.setCreationDate(LocalDateTime.now());
        request.setPlanedFinishDate(createRequestDto.getDeadline());
        request.setWorkType(createRequestDto.getWorkType());
        request.setPeriodical(createRequestDto.isPeriodical());
        User byLogin = userRepository.findByLogin(login);
        request.setCustomer(byLogin);
        ratingService.createRating(request);
        requestRepository.save(request);

    }

    public RequestDto updateRequest(String id, RequestDto requestDto) {
        Request request = requestRepository.findById(UUID.fromString(id)).orElseThrow();
        request.setTime(requestDto.getTime());
        request.setDescription(requestDto.getDescription());
        request.setPlanedFinishDate(requestDto.getDeadline());
        request.setPeriodical(requestDto.isPeriodical());
        request.setWorkType(requestDto.getWorkType());
        if (RequestStatus.FINISHED.equals(requestDto.getStatus())) {
            request.setActualFinishDate(requestDto.getEndDate());
            request.setStatus(requestDto.getStatus());
            if (request.getTeacherScore() == null) {
                request.setTeacherScore(requestDto.getTeacherScore());
            }
            if (request.getParticipantsScore() == null) {
                request.setParticipantsScore(requestDto.getParticipantsScore());
            }
        }
        Request save = requestRepository.save(request);
        return getRequestDto(save);

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
        String customerName = userDetailInfoService.getUserName(customer);
        dto.setCreatorName(customerName);
        User worker = request.getWorker();
        if (Objects.nonNull(worker)) {
            String workerName = userDetailInfoService.getUserName(worker);
            dto.setAssignedBy(workerName);
        }
        dto.setDeadline(request.getPlanedFinishDate());
        dto.setPeriodical(request.isPeriodical());
        dto.setWorkType(request.getWorkType());
        dto.setParticipantsScore(request.getParticipantsScore());
        dto.setTeacherScore(request.getTeacherScore());
        return dto;
    }

    public RequestDto findById(UUID uuid) {
        Request request = requestRepository.findById(uuid).orElseThrow();
        return getRequestDto(request);
    }

    public List<Request> findUnassigned() {
        List<Request> byStatus = requestRepository.findByStatus(RequestStatus.NEW);
        byStatus.forEach(request -> request.setStatus(RequestStatus.PLANNING));
        return byStatus;
    }

    public void updateList(List<Request> tempAssigned) {
        requestRepository.saveAll(tempAssigned);
    }

    public Page<Request> findByPrincipalAndStatusAndSubjectAreaAndCreationDateGraterThanPageable(Principal principal,
                                                                                                 Pageable pageable,
                                                                                                 List<RequestStatus> requestStatuses,
                                                                                                 List<SubjectArea> subjectArea, LocalDateTime start) {
        User user = userRepository.findByLogin(principal.getName());
        Role role = user.getRole();
        RoleType code = role.getCode();
        Page<Request> page = switch (code) {
            case STUDENT -> requestRepository.findByCustomerAndStatusInAndSubjectAreaInAndCreationDateGreaterThan(user, pageable, requestStatuses, subjectArea, start);
            case TEACHER -> requestRepository.findByWorkerAndStatusInAndSubjectAreaInAndCreationDateGreaterThan(user, pageable, requestStatuses, subjectArea, start);
            case ADMIN -> requestRepository.findByStatusInAndSubjectAreaInAndCreationDateGreaterThan(pageable, requestStatuses, subjectArea, start);
            case USER -> Page.empty();
        };

        return page;
    }

    public RequestDto changeStatus(UUID id, RequestStatus requestStatus) {
        Request byId = requestRepository.findById(id).orElseThrow();
        byId.setStatus(requestStatus);
        requestRepository.save(byId);
        return getRequestDto(byId);
    }

    public RequestDto setScores(UUID fromString, RequestDto requestDto) {
        Request request = requestRepository.findById(fromString).orElseThrow();
        request.setTeacherScore(request.getTeacherScore());
        request.setParticipantsScore(request.getParticipantsScore());
        requestRepository.save(request);
        return getRequestDto(request);
    }
}
