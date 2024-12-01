package com.example.digitalplatform.controller;

import com.example.digitalplatform.db.model.RoleType;
import com.example.digitalplatform.controller.dto.RoleDto;
import com.example.digitalplatform.controller.dto.UserAccountDto;
import com.example.digitalplatform.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
class AccountController {

    UserService userService;

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    String getAddAccountForm(Model model) {

        model.addAttribute("userData", new UserAccountDto());
        List<RoleDto> roles = userService.getAvailableRoles();
        model.addAttribute("roles", roles);
        return "accounts/addAccount";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    String addAccount(UserAccountDto userAccountDto, Model model) {
        userService.registerNewUserAccount(userAccountDto);
        List<UserAccountDto> accounts = userService.getAllUserAccounts();
        model.addAttribute("accounts", accounts);
        return "redirect:/accounts/all";
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    String getAll(@RequestParam("page") Optional<Integer> page,
                  @RequestParam("size") Optional<Integer> size,
                  @RequestParam(value = "role",required = false) RoleType roleCode,
                  Model model) {
        int start = page.orElse(1);
        int pageSize = size.orElse(5);
        PageRequest pageRequest = PageRequest.of(start - 1, pageSize);
        List<UserAccountDto> accounts = Objects.isNull(roleCode) ? userService.getAllUserAccounts()
                :userService.findByRoleCode(roleCode);
        int end = Math.min((start - 1 + pageRequest.getPageSize()), accounts.size());
        List<UserAccountDto> pageContent = accounts.isEmpty()? Collections.emptyList(): accounts.subList(start-1, end);
        Page<UserAccountDto> accountPage = new PageImpl<>(pageContent, pageRequest, accounts.size());
        model.addAttribute("accounts", accountPage);
        List<RoleDto> availableRoles = userService.getAvailableRoles();
        model.addAttribute("roles", availableRoles);
        model.addAttribute("roleCode", roleCode);
        return "accounts/accounts";
    }
}