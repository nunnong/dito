package com.ssafy.Dito.domain.groups.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "콕콕찌르기 요청")
public record PokeReq(

    @Schema(description = "찌를 대상 사용자 ID", example = "123", required = true)
    @NotNull(message = "대상 사용자 ID는 필수입니다")
    long targetUserId

) { }