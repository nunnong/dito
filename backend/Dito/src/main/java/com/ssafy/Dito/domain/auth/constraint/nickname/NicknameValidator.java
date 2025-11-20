package com.ssafy.Dito.domain.auth.constraint.nickname;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NicknameValidator implements ConstraintValidator<ValidNickname, String> {

    private static final String REGEX = "^[A-Za-z가-힣0-9]{1,7}$";

    @Override
    public boolean isValid(String nickname, ConstraintValidatorContext context) {
        if (nickname == null) {
            return false;
        }
        return nickname.matches(REGEX);
    }
}
