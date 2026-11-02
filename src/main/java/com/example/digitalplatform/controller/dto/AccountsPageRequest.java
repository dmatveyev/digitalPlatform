package com.example.digitalplatform.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * DTO-обёртка query-параметров page/size для AccountController.getAll.
 * Spring мапит query-параметры в поля, Bean Validation (@Min(1)/@Max) возвращает 400 при нарушении.
 */
@Data
public class AccountsPageRequest {

    @Min(1)
    int page = 1;

    @Min(1)
    @Max(100)
    int size = 5;

}
