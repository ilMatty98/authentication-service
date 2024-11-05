package com.ilmatty98.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxSizeValidator implements ConstraintValidator<MaxSize, String> {

    private long maxFileSize;

    @Override
    public void initialize(MaxSize constraintAnnotation) {
        maxFileSize = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.getBytes().length <= maxFileSize;
    }

}
