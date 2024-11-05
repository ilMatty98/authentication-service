package com.ilmatty98.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxSizeValidator.class)
public @interface MaxSize {

    String message() default "File size exceeds the maximum limit";

    long value();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
