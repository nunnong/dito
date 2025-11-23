package com.ssafy.Dito.domain.screentime.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 스크린타임 갱신 요청 DTO
 * 앱에서 5분마다 전송
 */
@Schema(description = "스크린타임 갱신 요청")
public record ScreenTimeUpdateReq(

    @Schema(description = "그룹 ID", example = "1")
    @NotNull(message = "그룹 ID는 필수입니다")
    Long groupId,

    @Schema(description = "날짜", example = "2025-01-07")
    @NotNull(message = "날짜는 필수입니다")
    LocalDate date,

    @Schema(description = "해당 날짜의 총 스크린타임 (분)", example = "120")
    @NotNull(message = "스크린타임은 필수입니다")
    @Min(value = 0, message = "스크린타임은 0 이상이어야 합니다")
    Integer totalMinutes,


    @Schema(description = "해당 날짜의 총 유튜브 스크린타임 (분)", example = "120")
    @Min(value = 0, message = "스크린타임은 0 이상이어야 합니다")
    Integer youtubeMinutes
) {
}
