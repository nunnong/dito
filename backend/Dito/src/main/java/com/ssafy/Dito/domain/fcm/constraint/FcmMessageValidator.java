package com.ssafy.Dito.domain.fcm.constraint;

import com.ssafy.Dito.domain.fcm.dto.FcmSendRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * FCM 메시지 요청의 유효성을 검증하는 Validator
 * - fcm_type이 notification, data, mixed 중 하나인지 검증
 * - notification 또는 mixed 타입일 때 title이 필수인지 검증
 */
public class FcmMessageValidator implements ConstraintValidator<ValidFcmMessage, FcmSendRequest> {

    @Override
    public void initialize(ValidFcmMessage constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(FcmSendRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return false;
        }

        String fcmType = request.fcmType();

        // 1. fcmType이 null이거나 빈 문자열인지 체크
        if (fcmType == null || fcmType.isBlank()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "fcm_type은 필수입니다."
            ).addPropertyNode("fcmType").addConstraintViolation();
            return false;
        }

        // 2. fcmType이 올바른 값인지 검증
        if (!fcmType.equals("notification") &&
            !fcmType.equals("data") &&
            !fcmType.equals("mixed")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "fcm_type은 'notification', 'data', 'mixed' 중 하나여야 합니다."
            ).addPropertyNode("fcmType").addConstraintViolation();
            return false;
        }

        // 3. notification 또는 mixed 타입일 때 title 필수 검증
        if ((fcmType.equals("notification") || fcmType.equals("mixed"))) {
            if (request.title() == null || request.title().isBlank()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "notification 또는 mixed 타입일 때 title은 필수입니다."
                ).addPropertyNode("title").addConstraintViolation();
                return false;
            }
        }

        // 4. data 타입일 때는 data 맵이 필수
        if (fcmType.equals("data")) {
            if (request.data() == null || request.data().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "data 타입일 때 data 필드는 필수입니다."
                ).addPropertyNode("data").addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}