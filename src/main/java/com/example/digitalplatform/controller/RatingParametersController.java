package com.example.digitalplatform.controller;

import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/rating_parameters")
@RequiredArgsConstructor
public class RatingParametersController {
    private final RatingParametersRepository ratingParametersRepository;

    @GetMapping("/all")
    public String getAll(Model model) {
        List<RatingParameters> all = ratingParametersRepository.findAll();
        model.addAttribute("rating_parameters", all);
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
    public String create(RatingParameters toAdd, Model model) {
        RatingParameters newParam = ratingParametersRepository.save(toAdd);
        List<RatingParameters> list = ratingParametersRepository.findAll();
        model.addAttribute("rating_parameters", list);
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
