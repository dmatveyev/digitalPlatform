package com.example.digitalplatform.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO-обёртка query-параметра id для RatingParametersController.delete.
 * Spring мапит query-параметр в поле, Bean Validation (@NotBlank) возвращает 400 при пустом/отсутствующем значении.
 */
@Data
public class RatingParametersDeleteRequest {

    @NotBlank
    String id;

}
