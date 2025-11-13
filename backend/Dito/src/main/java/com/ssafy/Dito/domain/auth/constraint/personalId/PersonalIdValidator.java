package com.ssafy.Dito.domain.auth.constraint.personalId;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class
PersonalIdValidator implements ConstraintValidator<ValidPersonalId, String> {

    private static final String PERSONAL_ID_REGEX = "^[a-z][a-z0-9]{3,19}$";

    @Override
    public boolean isValid(String personalId, ConstraintValidatorContext context) {
        if (personalId == null || personalId.isEmpty()) {
            return false;
        }

        if (!Pattern.matches(PERSONAL_ID_REGEX, personalId)) {
            return false;
        }

        if (personalId.matches("^[0-9]+$")) {
            return false;
        }

        return true;
    }
}
