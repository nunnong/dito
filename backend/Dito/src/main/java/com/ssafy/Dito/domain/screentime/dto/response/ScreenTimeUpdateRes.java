package com.ssafy.Dito.domain.screentime.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 스크린타임 갱신 응답 DTO
 */
@Schema(description = "스크린타임 갱신 응답")
public record ScreenTimeUpdateRes(

    @Schema(description = "그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "사용자 ID", example = "10")
    Long userId,

    @Schema(description = "날짜", example = "2025-01-07")
    LocalDate date,

    @Schema(description = "총 스크린타임 (분)", example = "120")
    Integer totalMinutes,

    @Schema(description = "처리 상태", example = "updated")
    String status
) {
    public static ScreenTimeUpdateRes of(Long groupId, Long userId, LocalDate date, Integer totalMinutes, String status) {
        return new ScreenTimeUpdateRes(groupId, userId, date, totalMinutes, status);
    }
}
