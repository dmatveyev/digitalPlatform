package com.example.digitalplatform.controller;

import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.RequestStatus;
import com.example.digitalplatform.db.model.SubjectArea;
import com.example.digitalplatform.db.model.WorkType;
import com.example.digitalplatform.db.repository.RequestRepository;
import com.example.digitalplatform.db.repository.SubjectAreaRepository;
import com.example.digitalplatform.dto.CreateRequestDto;
import com.example.digitalplatform.dto.RequestDto;
import com.example.digitalplatform.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
public class RequestsController {

    private final RequestRepository requestRepository;
    private final SubjectAreaRepository subjectAreaRepository;
    private final RequestService requestService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public String getAll(@RequestParam("page") Optional<Integer> page,
                         @RequestParam("size") Optional<Integer> size,
                         @RequestParam(value = "status", required = false) RequestStatus requestStatus,
                         @RequestParam(value = "subjectArea", required = false) String subjectArea,
                         @RequestParam(value = "startDate", required = false) String startDate,
                         Model model, Principal principal) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(5);
        List<RequestStatus> requestStatuses = requestStatus == null ?
                Arrays.stream(RequestStatus.values()).toList(): List.of(requestStatus);
        List<SubjectArea> subjectAreas = subjectAreaRepository.findAll();
        List<SubjectArea> searchAreas = subjectArea == null || subjectArea.isEmpty() ? subjectAreas :
                List.of(subjectAreaRepository.findById(UUID.fromString(subjectArea)).orElseThrow());
        LocalDateTime start = startDate == null || startDate.isEmpty()? LocalDateTime.now().minusDays(30) :
                LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        Page<Request> result = requestService.findByPrincipalAndStatusAndSubjectAreaAndCreationDateGraterThanPageable(principal,
                PageRequest.of(currentPage - 1, pageSize), requestStatuses, searchAreas, start);
        model.addAttribute("requestPage", result);
        model.addAttribute("statuses", RequestStatus.values());
        model.addAttribute("selectedSt", requestStatus);
        model.addAttribute("subjectAreas", subjectAreas);
        model.addAttribute("area", subjectArea);
        model.addAttribute("startDate", startDate);
        return "requests/requests";
    }

    @GetMapping("/edit")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public String edit(@RequestParam("id") String id, Model model) {
        RequestDto requestDto = requestService.findById(UUID.fromString(id));
        model.addAttribute("request", requestDto);
        model.addAttribute("workTypes", Arrays.stream(WorkType.values()).toList());
        return "requests/editRequest";
    }

    @PostMapping("/edit")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public String save(@RequestParam("id") String id, RequestDto createRequestDto, Model model, Principal
            principal) {
        RequestDto save = requestService.updateRequest(id, createRequestDto);
        List<RequestDto> requests = requestService.findByPrincipal(principal);
        model.addAttribute("requests", requests);
        return "redirect:/requests/all?page=1&size=5";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    public String create(CreateRequestDto createRequestDto, Model model, Principal principal) {
        requestService.addRequest(createRequestDto, principal.getName());

        List<RequestDto> requests = requestService.findByPrincipal(principal);
        model.addAttribute("requests", requests);
        return "redirect:/requests/all?page=1&size=5";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    public String showCreateForm(Model model) {
        model.addAttribute("request", new CreateRequestDto());
        List<SubjectArea> all = subjectAreaRepository.findAll();
        model.addAttribute("subjectAreas", all);
        model.addAttribute("workTypes", Arrays.stream(WorkType.values()).toList());
        return "requests/addRequest";
    }

    @GetMapping("/delete")
    @PreAuthorize("hasRole('STUDENT')")
    public String delete(@RequestParam("id") String id, Model model, Principal principal) {
        requestRepository.deleteById(UUID.fromString(id));
        List<RequestDto> list = requestService.findByPrincipal(principal);
        model.addAttribute("requests", list);
        return "redirect:/requests/all?page=1&size=5";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public String changeStatus(@PathVariable("id") String id,
                               @RequestParam("status") RequestStatus requestStatus,
                               RequestDto requestDto,
                               Model model,
                               Principal principal) {
        RequestDto updated = requestService.changeStatus(UUID.fromString(id), requestDto.getStatus());
        model.addAttribute("request", updated);
        return "redirect:/requests/edit?id="+id;
    }

}
