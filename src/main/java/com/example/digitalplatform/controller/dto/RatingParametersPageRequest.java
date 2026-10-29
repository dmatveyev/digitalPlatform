package com.example.digitalplatform.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * DTO-обёртка query-параметров page/size для RatingParametersController.getAll.
 * Spring мапит query-параметры в поля, Bean Validation (@Min(1)) возвращает 400 при нарушении.
 */
@Data
public class RatingParametersPageRequest {

    @Min(1)
    int page = 1;

    @Min(1)
    @Max(100)
    int size = 5;

}
