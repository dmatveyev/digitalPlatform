package com.example.digitalplatform.db.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint: minValue не должно быть больше maxValue.
 * Невалидные значения отклоняются с 400 (MethodArgumentNotValidException).
 */
@Documented
@Constraint(validatedBy = MinNotGreaterThanMaxValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinNotGreaterThanMax {

    String message() default "minValue must not be greater than maxValue";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
