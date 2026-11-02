package com.example.digitalplatform.controller;

import com.example.digitalplatform.controller.dto.AccountsPageRequest;
import com.example.digitalplatform.controller.dto.RoleDto;
import com.example.digitalplatform.controller.dto.UserAccountDto;
import com.example.digitalplatform.db.model.RoleType;
import com.example.digitalplatform.service.UserService;
import jakarta.validation.Valid;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Тонкий MVC-контроллер пользовательских аккаунтов: маппинг DTO + redirect.
 * Вся бизнес-логика (валидация, создание, пагинация) вынесена в {@link UserService}.
 */
@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AccountController {

    private final UserService userService;

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    String getAddAccountForm(Model model) {
        model.addAttribute("userData", new UserAccountDto());
        addRoleOptions(model);
        return "accounts/addAccount";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    String addAccount(@Valid UserAccountDto userAccountDto) {
        userService.registerNewUserAccount(userAccountDto);
        return "redirect:/accounts/all";
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    String getAll(@Valid AccountsPageRequest accountsPageRequest,
                  @RequestParam(value = "role", required = false) RoleType roleCode,
                  Model model) {
        Pageable pageRequest = PageRequest.of(accountsPageRequest.getPage() - 1, accountsPageRequest.getSize());
        Page<UserAccountDto> accountPage = roleCode == null ? userService.getAllUserAccounts(pageRequest)
                : userService.findByRoleCode(roleCode, pageRequest);
        model.addAttribute("accounts", accountPage);
        addRoleOptions(model);
        model.addAttribute("roleCode", roleCode);
        return "accounts/accounts";
    }

    /**
     * Кладёт список доступных ролей в модель один раз на запрос.
     */
    private void addRoleOptions(Model model) {
        List<RoleDto> roles = userService.getAvailableRoles();
        model.addAttribute("roles", roles);
    }
}