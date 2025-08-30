package com.nisum.user.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordPolicyValidator.class)
public @interface PasswordPolicy {
    String message() default "The password must be 8 to 20 characters long, include at least one uppercase letter,"
            + " one lowercase letter, one digit, and one symbol, and must not contain spaces.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String property() default "app.password.regex";
}
