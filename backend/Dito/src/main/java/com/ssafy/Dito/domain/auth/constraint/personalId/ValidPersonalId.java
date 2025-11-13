package com.ssafy.Dito.domain.auth.constraint.personalId;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PersonalIdValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPersonalId {
    String message() default
        "아이디는 영문 소문자로 시작하며, 영문 소문자와 숫자 조합 4~20자여야 합니다. (숫자만 불가)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}