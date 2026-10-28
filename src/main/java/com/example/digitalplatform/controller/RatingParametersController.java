package com.example.digitalplatform.controller;

import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/rating_parameters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RatingParametersController {
    private final RatingParametersRepository ratingParametersRepository;

    @GetMapping("/all")
    public String getAll(
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            Model model) {
        int start = page.orElse(1);
        int pageSize = size.orElse(5);
        PageRequest pageRequest = PageRequest.of(start - 1, pageSize);
        List<RatingParameters> all = ratingParametersRepository.findAll();
        int end = Math.min((start-1 + pageRequest.getPageSize()), all.size());
        List<RatingParameters> pageContent = all.isEmpty()? Collections.emptyList(): all.subList(start-1, end);
        Page<RatingParameters> pageItem = new PageImpl<>(pageContent, pageRequest, all.size());

        model.addAttribute("pageItem", pageItem);
        return "rating_parameters/rating_parameters";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam("id") String id, Model model) {
        RatingParameters ratingParameters = ratingParametersRepository.findById(UUID.fromString(id)).orElseThrow(NullPointerException::new);
        model.addAttribute("rating_parameters", ratingParameters);
        return "rating_parameters/rating_parameters_edit";
    }

    @PostMapping("/edit")
    public String save(@RequestParam("id") String id, RatingParameters toSave, Model model) {
        RatingParameters ratingParameters = ratingParametersRepository.findById(UUID.fromString(id)).orElseThrow();
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter with code '" + toAdd.getCode() + "' already exists");
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
    public String delete(@RequestParam("id") String id, Model model) {
        ratingParametersRepository.deleteById(UUID.fromString(id));
        List<RatingParameters> list = ratingParametersRepository.findAll();
        model.addAttribute("rating_parameters", list);
        return "redirect:/rating_parameters/all";
    }
}
