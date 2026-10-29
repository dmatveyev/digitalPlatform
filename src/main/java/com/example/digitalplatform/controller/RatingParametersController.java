package com.example.digitalplatform.controller;

import com.example.digitalplatform.controller.dto.RatingParametersDeleteRequest;
import com.example.digitalplatform.controller.dto.RatingParametersPageRequest;
import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.service.RatingParametersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

/**
 * Тонкий MVC-контроллер параметров рейтинга: маппинг DTO + redirect.
 * Вся CRUD-бизнес-логика вынесена в {@link RatingParametersService}.
 */
@Controller
@RequestMapping("/rating_parameters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RatingParametersController {

    private final RatingParametersService ratingParametersService;

    @GetMapping("/all")
    public String getAll(@Valid @ModelAttribute RatingParametersPageRequest request, Model model) {
        Page<RatingParameters> pageItem = ratingParametersService.getPaged(request.getPage(), request.getSize());

        model.addAttribute("pageItem", pageItem);
        return "rating_parameters/rating_parameters";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam("id") UUID id, Model model) {
        // Парсинг/валидация формата UUID передана Spring (@RequestParam UUID):
        // мусорный id -> TypeMismatchException -> 400 (SimpleErrorController -> 400page.html);
        // валидный id без записи в БД -> 404 из сервиса (SimpleErrorController -> 404page.html).
        final RatingParameters ratingParameters = ratingParametersService.findByIdOrThrow(id);
        model.addAttribute("rating_parameters", ratingParameters);
        return "rating_parameters/rating_parameters_edit";
    }

    @PostMapping("/edit")
    public String save(@RequestParam("id") UUID id, @Valid RatingParameters toSave) {
        // Bean Validation (@NotBlank на code/description в RatingParameters + class-level constraint
        // @MinNotGreaterThanMax) проверяет входные параметры. Если валидация не пройдена —
        // Spring выбрасывает MethodArgumentNotValidException (400), который диспетчеризуется в /error
        // и обрабатывается SimpleErrorController -> HTML-страница 400page.
        // Парсинг/валидация формата UUID передана Spring (@RequestParam UUID): мусорный id -> 400.
        ratingParametersService.save(id, toSave);
        return "redirect:/rating_parameters/all";
    }

    @PostMapping("/create")
    public String create(@Valid RatingParameters toAdd) {
        // Bean Validation (@NotBlank на code/description в RatingParameters + class-level constraint
        // @MinNotGreaterThanMax) проверяет входные параметры. Если валидация не пройдена —
        // Spring выбрасывает MethodArgumentNotValidException (400), который диспетчеризуется в /error
        // и обрабатывается SimpleErrorController -> HTML-страница 400page.

        // Уникальность кода параметра: повторная запись с тем же code отклоняется с 409 Conflict
        // (ResponseStatusException из сервиса), чтобы не создавать дубликаты.
        ratingParametersService.create(toAdd);
        return "redirect:/rating_parameters/all";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("rating_parameters", new RatingParameters());
        return "rating_parameters/add_rating_parameters";
    }

    @PostMapping("/delete")
    public String delete(@Valid @ModelAttribute RatingParametersDeleteRequest request) {
        // Бизнес-логика удаления (парсинг UUID, проверка существования, удаление) вынесена в сервис.
        // Bean Validation (@NotBlank на id в DTO) возвращает 400 для пустого/отсутствующего id;
        // невалидный UUID -> 400, отсутствующая запись -> 404 (ResponseStatusException из сервиса).
        ratingParametersService.deleteById(request.getId());
        return "redirect:/rating_parameters/all";
    }
}
