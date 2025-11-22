package com.ssafy.Dito.domain.screentime.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 현재 앱 사용 정보 업데이트 요청 DTO
 */
@Schema(description = "현재 앱 사용 정보 업데이트 요청")
public record UpdateCurrentAppReq(

    @Schema(description = "그룹 ID", example = "1", required = true)
    @NotNull(message = "그룹 ID는 필수입니다")
    Long groupId,

    @Schema(description = "앱 패키지명", example = "com.google.android.youtube", required = true)
    @NotBlank(message = "앱 패키지명은 필수입니다")
    String appPackage,

    @Schema(description = "앱 이름", example = "YouTube", required = true)
    @NotBlank(message = "앱 이름은 필수입니다")
    String appName,

    @Schema(description = "사용 시간 (초)", example = "600", required = false)
    Long usageDuration
) {
}
