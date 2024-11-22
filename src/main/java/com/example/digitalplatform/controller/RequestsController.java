package com.example.digitalplatform.controller;

import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.SubjectArea;
import com.example.digitalplatform.db.repository.RequestRepository;
import com.example.digitalplatform.db.repository.SubjectAreaRepository;
import com.example.digitalplatform.dto.RequestDto;
import com.example.digitalplatform.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class RequestsController {

   private final RequestRepository authorService;
   private final SubjectAreaRepository subjectAreaRepository;
   private final RequestService requestService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('STUDENT')")
    public String getAll(Model model, Principal principal) {
        List<Request> list = authorService.findAll();
        model.addAttribute("requests", list);
        return "requests/requests";
    }

    @GetMapping("/edit")
    @PreAuthorize("hasRole('STUDENT')")
    public String edit(@RequestParam("id") String id, Model model) {
        Request Request = authorService.findById(UUID.fromString(id)).orElseThrow(NullPointerException::new);
        model.addAttribute("request", Request);
        return "requests/editRequest";
    }

    @PostMapping("/edit")
    @PreAuthorize("hasRole('STUDENT')")
    public String save(@RequestParam("id") String id, RequestDto requestDto, Model model) {
        Request save = requestService.updateRequest(id, requestDto);
        model.addAttribute("requests", save);
        return "redirect:/requests/all";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    public String create(RequestDto requestDto, Model model, Principal principal) {
        requestService.addRequest(requestDto, principal.getName());

        List<Request> allGenres = authorService.findAll();
        model.addAttribute("requests", allGenres);
        return "redirect:/requests/all";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    public String showCreateForm(Model model) {
        model.addAttribute("request", new Request());
        model.addAttribute("requestDto", new RequestDto());
        List<SubjectArea> all = subjectAreaRepository.findAll();
        model.addAttribute("subjectAreas", all);
        return "requests/addRequest";
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('STUDENT')")
    public String delete(@RequestParam("id") String id, Model model) {
        authorService.deleteById(UUID.fromString(id));
        List<Request> list = authorService.findAll();
        model.addAttribute("requests", list);
        return "redirect:/requests/all";
    }

}
