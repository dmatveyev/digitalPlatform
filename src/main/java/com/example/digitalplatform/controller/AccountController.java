package com.example.digitalplatform.controller;

import com.example.digitalplatform.dto.UserAccountDto;
import com.example.digitalplatform.dto.UserType;
import com.example.digitalplatform.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ROLE_ADMIN')")
class AccountController {

    UserService userService;

    @GetMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    String getAddAccountForm(Model model) {

        model.addAttribute("userData", new UserAccountDto());
        model.addAttribute("types", Arrays.stream(UserType.values()).toList());
        return "accounts/addAccount";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    String addAccount(UserAccountDto userAccountDto, Model model) {
        userService.registerNewUserAccount(userAccountDto);
        List<UserAccountDto> accounts = userService.getAllUserAccounts();
        model.addAttribute("accounts", accounts);
        return "redirect:/accounts/all";
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    String getAll(Model model) {
        List<UserAccountDto> accounts = userService.getAllUserAccounts();
        model.addAttribute("accounts", accounts);
        return "accounts/accounts";
    }
}