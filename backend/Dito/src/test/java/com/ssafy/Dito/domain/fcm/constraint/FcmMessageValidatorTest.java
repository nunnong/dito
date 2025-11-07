package com.ssafy.Dito.domain.fcm.constraint;

import com.ssafy.Dito.domain.fcm.dto.FcmSendRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FCM Message Validator 테스트")
class FcmMessageValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("notification 타입 - title 있을 때 검증 성공")
    void testValidNotificationType() {
        // given
        FcmSendRequest request = new FcmSendRequest(
            "user123",
            "메시지 내용",
            null,
            "intervention",
            FcmSendRequest.TYPE_NOTIFICATION,
            "디토",  // title 필수
            null
        );

        // when
        Set<ConstraintViolation<FcmSendRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("data 타입 - data 있을 때 검증 성공")
    void testValidDataType() {
        // given
        Map<String, String> data = Map.of(
            "key1", "value1",
            "key2", "value2"
        );

        FcmSendRequest request = new FcmSendRequest(
            "user123",
            "메시지 내용",
            null,
            "intervention",
            FcmSendRequest.TYPE_DATA,
            null,  // data 타입은 title 불필요
            data   // data 필수
        );

        // when
        Set<ConstraintViolation<FcmSendRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("mixed 타입 - title과 data 모두 있을 때 검증 성공")
    void testValidMixedType() {
        // given
        Map<String, String> data = Map.of(
            "action", "rest",
            "mission_type", "REST"
        );

        FcmSendRequest request = new FcmSendRequest(
            "user123",
            "메시지 내용",
            42L,
            "intervention",
            FcmSendRequest.TYPE_MIXED,
            "디토",  // title 필수
            data
        );

        // when
        Set<ConstraintViolation<FcmSendRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("잘못된 fcm_type - 검증 실패")
    void testInvalidFcmType() {
        // given
        FcmSendRequest request = new FcmSendRequest(
            "user123",
            "메시지 내용",
            null,
            "intervention",
            "invalid_type",  // 잘못된 타입
            "디토",
            null
        );

        // when
        Set<ConstraintViolation<FcmSendRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("fcm_type은 'notification', 'data', 'mixed' 중 하나여야 합니다.");
    }

    @Test
    @DisplayName("notification 타입 - title 없을 때 검증 실패")
    void testNotificationWithoutTitle() {
        // given
        FcmSendRequest request = new FcmSendRequest(
            "user123",
            "메시지 내용",
            null,
            "intervention",
            FcmSendRequest.TYPE_NOTIFICATION,
            null,  // title 누락 (필수)
            null
        );

        // when
        Set<ConstraintViolation<FcmSendRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("notification 또는 mixed 타입일 때 title은 필수입니다.");
    }

    @Test
    @DisplayName("mixed 타입 - title 없을 때 검증 실패")
    void testMixedWithoutTitle() {
        // given
        Map<String, String> data = Map.of("key", "value");

        FcmSendRequest request = new FcmSendRequest(
            "user123",
            "메시지 내용",
            null,
            "intervention",
            FcmSendRequest.TYPE_MIXED,
            null,  // title 누락 (필수)
            data
        );

        // when
        Set<ConstraintViolation<FcmSendRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("notification 또는 mixed 타입일 때 title은 필수입니다.");
    }

    @Test
    @DisplayName("data 타입 - data 없을 때 검증 실패")
    void testDataWithoutData() {
        // given
        FcmSendRequest request = new FcmSendRequest(
            "user123",
            "메시지 내용",
            null,
            "intervention",
            FcmSendRequest.TYPE_DATA,
            null,
            null  // data 누락 (필수)
        );

        // when
        Set<ConstraintViolation<FcmSendRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("data 타입일 때 data 필드는 필수입니다.");
    }

    @Test
    @DisplayName("data 타입 - title 없어도 검증 성공")
    void testDataTypeWithoutTitle() {
        // given
        Map<String, String> data = Map.of(
            "action", "rest",
            "type", "intervention"
        );

        FcmSendRequest request = new FcmSendRequest(
            "user123",
            "메시지 내용",
            null,
            "intervention",
            FcmSendRequest.TYPE_DATA,
            null,  // data 타입은 title 없어도 OK
            data
        );

        // when
        Set<ConstraintViolation<FcmSendRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("fcm_type이 null일 때 검증 실패")
    void testNullFcmType() {
        // given
        FcmSendRequest request = new FcmSendRequest(
            "user123",
            "메시지 내용",
            null,
            "intervention",
            null,  // fcm_type null
            "디토",
            null
        );

        // when
        Set<ConstraintViolation<FcmSendRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        // @NotBlank 어노테이션으로 인한 검증 실패
    }

    @Test
    @DisplayName("필수 필드 누락 검증")
    void testRequiredFields() {
        // given
        FcmSendRequest request = new FcmSendRequest(
            null,   // personalId 누락
            null,   // message 누락
            null,
            null,   // type 누락
            null,   // fcm_type 누락
            null,
            null
        );

        // when
        Set<ConstraintViolation<FcmSendRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("mission_id가 있는 mixed 타입 검증 성공")
    void testMixedTypeWithMissionId() {
        // given
        Map<String, String> data = Map.of(
            "mission_type", "REST",
            "duration", "300"
        );

        FcmSendRequest request = new FcmSendRequest(
            "user123",
            "잠시 휴식을 취해보세요",
            42L,  // mission_id
            "intervention",
            FcmSendRequest.TYPE_MIXED,
            "디토",
            data
        );

        // when
        Set<ConstraintViolation<FcmSendRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
        assertThat(request.missionId()).isEqualTo(42L);
    }
}