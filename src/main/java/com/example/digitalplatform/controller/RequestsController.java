package com.example.digitalplatform.controller;

import com.example.digitalplatform.db.model.Request;
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
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                         Model model, Principal principal) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(5);
        Page<Request> result = requestService.findByPrincipalPageable(principal, PageRequest.of(currentPage - 1, pageSize));
        model.addAttribute("requestPage", result);
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
    @PreAuthorize("hasRole('STUDENT')")
    public String save(@RequestParam("id") String id, CreateRequestDto createRequestDto, Model model, Principal
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

    @PostMapping("/delete")
    @PreAuthorize("hasRole('STUDENT')")
    public String delete(@RequestParam("id") String id, Model model, Principal principal) {
        requestRepository.deleteById(UUID.fromString(id));
        List<RequestDto> list = requestService.findByPrincipal(principal);
        model.addAttribute("requests", list);
        return "redirect:/requests/all?page=1&size=5";
    }

}
