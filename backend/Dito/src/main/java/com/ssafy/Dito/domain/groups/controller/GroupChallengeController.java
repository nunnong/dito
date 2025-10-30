package com.ssafy.Dito.domain.groups.controller;

import com.ssafy.Dito.domain.groups.dto.request.CreateGroupChallengeReq;
import com.ssafy.Dito.domain.groups.dto.response.GroupChallengeRes;
import com.ssafy.Dito.domain.groups.service.GroupChallengeService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import com.ssafy.Dito.global.dto.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Group Challenge", description = "그룹 챌린지 API")
@RestController
@RequestMapping("/challenges/groups")
@RequiredArgsConstructor
public class GroupChallengeController {

    private final GroupChallengeService groupChallengeService;

    @Operation(
        summary = "그룹 챌린지 생성",
        description = "새로운 그룹 챌린지를 생성합니다. 초대 코드가 자동으로 생성됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "그룹 챌린지 생성 성공",
            content = @Content(schema = @Schema(implementation = GroupChallengeRes.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검증 실패)",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "error": true,
                      "message": "잘못된 요청입니다. 입력값을 확인해주세요."
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "서버 오류 (초대 코드 생성 실패 등)",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "error": true,
                      "message": "서버 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @PostMapping
    public ResponseEntity<SingleResult<GroupChallengeRes>> createGroupChallenge(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "그룹 챌린지 생성 요청",
            required = true,
            content = @Content(schema = @Schema(implementation = CreateGroupChallengeReq.class))
        )
        @Valid @RequestBody CreateGroupChallengeReq request,

        @Parameter(
            description = "사용자 ID (임시, JWT 구현 후 제거 예정)",
            required = true,
            example = "1"
        )
        @RequestHeader("X-User-Id") Long userId
    ) {
        GroupChallengeRes response = groupChallengeService.createGroupChallenge(request, userId);
        return ApiResponse.create(response);
    }
}
