package com.ssafy.Dito.domain.auth.constraint.nickname;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NicknameValidator implements ConstraintValidator<ValidNickname, String> {

    private static final String REGEX = "^[A-Za-z가-힣]{1,7}$";

    @Override
    public boolean isValid(String nickname, ConstraintValidatorContext context) {
        if (nickname == null) {
            return false; // null 자체가 허용되면 @NotNull을 따로 붙여야 함
        }
        return nickname.matches(REGEX);
    }
}
