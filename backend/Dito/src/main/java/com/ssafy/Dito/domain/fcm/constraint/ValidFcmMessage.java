package com.ssafy.Dito.domain.fcm.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * FCM 메시지 요청의 유효성을 검증하는 커스텀 어노테이션
 * - fcm_type이 notification, data, mixed 중 하나인지 검증
 * - notification 또는 mixed 타입일 때 title이 필수인지 검증
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FcmMessageValidator.class)
@Documented
public @interface ValidFcmMessage {
    String message() default "FCM 메시지 형식이 올바르지 않습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}