package com.example.digitalplatform.controller;

import com.example.digitalplatform.db.model.RoleType;
import com.example.digitalplatform.db.model.SubjectArea;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.model.WorkType;
import com.example.digitalplatform.db.repository.SubjectAreaRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.dto.CreateRequestDto;
import com.example.digitalplatform.dto.RequestDto;
import com.example.digitalplatform.dto.UserAccountDto;
import com.example.digitalplatform.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class ProfileController {

    UserService userService;
    SubjectAreaRepository subjectAreaRepository;
    UserRepository userRepository;


    @GetMapping("/edit")
    public String edit(Model model, Principal principal) {
        String name = principal.getName();
        UserAccountDto userInfoByUserName = userService.getUserInfoByUserName(name);
        List<SubjectArea> all = subjectAreaRepository.findAll();
        model.addAttribute("info", userInfoByUserName);
        model.addAttribute("areas", all);
        return "accounts/profile";
    }

    @PostMapping("/edit")
    public String save(UserAccountDto dto, Principal principal) {
        String name = principal.getName();
        User byLogin = userRepository.findByLogin(name);
        userService.saveUserInfo(dto, byLogin);
        RoleType code = byLogin.getRole().getCode();
        String redirect = switch (code) {
            case ADMIN ->  "redirect:/accounts/all";
            default -> "redirect:/requests/all";
        };
        return redirect;
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('STUDENT')")
    public String delete(@RequestParam("id") String id, Model model, Principal principal) {
       /* requestRepository.deleteById(UUID.fromString(id));
        List<RequestDto> list = requestService.findByPrincipal(principal);
        model.addAttribute("requests", list);*/
        return "redirect:/requests/all";
    }
}