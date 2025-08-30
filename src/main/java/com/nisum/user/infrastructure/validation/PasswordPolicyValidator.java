package com.nisum.user.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordPolicyValidator implements ConstraintValidator<PasswordPolicy, String> {

    @Value("${app.password.regex}")
    private String regex;

    private Pattern pattern;

    @Override
    public void initialize(PasswordPolicy constraintAnnotation) {
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        return pattern.matcher(value).matches();
    }
}
