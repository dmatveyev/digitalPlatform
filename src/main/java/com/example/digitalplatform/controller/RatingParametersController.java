package com.example.digitalplatform.controller;

import com.example.digitalplatform.controller.dto.RatingParametersDeleteRequest;
import com.example.digitalplatform.controller.dto.RatingParametersPageRequest;
import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import com.example.digitalplatform.service.RatingParametersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/rating_parameters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RatingParametersController {
    private final RatingParametersRepository ratingParametersRepository;
    private final RatingParametersService ratingParametersService;

    @GetMapping("/all")
    public String getAll(@Valid @ModelAttribute RatingParametersPageRequest request, Model model) {
        Page<RatingParameters> pageItem = ratingParametersRepository.findAll(
                PageRequest.of(request.getPage() - 1, request.getSize(), Sort.by("code")));

        model.addAttribute("pageItem", pageItem);
        return "rating_parameters/rating_parameters";
    }

    private UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid rating parameter id: '" + id + "'");
        }
    }

    @GetMapping("/edit")
    public String edit(@RequestParam("id") String id, Model model) {
        final UUID uuid = parseId(id);
        RatingParameters ratingParameters = ratingParametersRepository.findById(uuid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating parameter with id '" + id + "' not found"));
        model.addAttribute("rating_parameters", ratingParameters);
        return "rating_parameters/rating_parameters_edit";
    }

    @PostMapping("/edit")
    public String save(@RequestParam("id") String id, @Valid RatingParameters toSave, Model model) {
        final UUID uuid = parseId(id);
        RatingParameters ratingParameters = ratingParametersRepository.findById(uuid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating parameter with id '" + id + "' not found"));
        ratingParameters.setCoefficient(toSave.getCoefficient());
        ratingParameters.setMaxValue(toSave.getMaxValue());
        ratingParameters.setMinValue(toSave.getMinValue());
        ratingParameters.setDescription(toSave.getDescription());
        RatingParameters save = ratingParametersRepository.save(ratingParameters);
        model.addAttribute("rating_parameters", save);
        return "redirect:/rating_parameters/all";
    }

    @PostMapping("/create")
    public String create(@Valid RatingParameters toAdd, Model model) {
        // Bean Validation (@NotBlank на code/description в RatingParameters) проверяет входные параметры.
        // Если валидация не пройдена — Spring выбрасывает MethodArgumentNotValidException (400),
        // который диспетчеризуется в /error и обрабатывается SimpleErrorController -> HTML-страница 400page.

        // Уникальность кода параметра: повторная запись с тем же code отклоняется с 400 Bad Request,
        // чтобы не создавать дубликаты. Исключение диспетчеризуется в /error -> SimpleErrorController.
        if (ratingParametersRepository.existsByCode(toAdd.getCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Parameter with code '" + toAdd.getCode() + "' already exists");
        }

        ratingParametersRepository.save(toAdd);
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
