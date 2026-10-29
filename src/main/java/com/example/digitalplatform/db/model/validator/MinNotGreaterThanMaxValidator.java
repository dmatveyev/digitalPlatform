package com.example.digitalplatform.db.model.validator;

import com.example.digitalplatform.db.model.RatingParameters;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Валидатор для @MinNotGreaterThanMax: проверяет, что minValue <= maxValue.
 */
public class MinNotGreaterThanMaxValidator implements ConstraintValidator<MinNotGreaterThanMax, RatingParameters> {

    @Override
    public boolean isValid(RatingParameters parameters, ConstraintValidatorContext context) {
        if (parameters == null) {
            return true; // null-объект обрабатывается другими constraint'ами (@NotNull и т.п.)
        }
        float min = parameters.getMinValue();
        float max = parameters.getMaxValue();

        // NaN не проходит сравнения — считаем невалидным значением (400).
        if (Float.isNaN(min) || Float.isNaN(max)) {
            return false;
        }
        if (min > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("minValue (" + min + ") must not be greater than maxValue (" + max + ")")
                    .addPropertyNode("minValue")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
