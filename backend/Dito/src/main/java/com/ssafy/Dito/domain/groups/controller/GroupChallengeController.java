package com.ssafy.Dito.domain.groups.controller;

import com.ssafy.Dito.domain.groups.dto.request.CreateGroupChallengeReq;
import com.ssafy.Dito.domain.groups.dto.request.GroupParticipantReq;
import com.ssafy.Dito.domain.groups.dto.request.JoinGroupReq;
import com.ssafy.Dito.domain.groups.dto.request.PokeReq;
import com.ssafy.Dito.domain.groups.dto.response.GroupChallengeRes;
import com.ssafy.Dito.domain.groups.dto.response.GroupDetailRes;
import com.ssafy.Dito.domain.groups.dto.response.GroupParticipantsRes;
import com.ssafy.Dito.domain.groups.dto.response.JoinGroupRes;
import com.ssafy.Dito.domain.groups.dto.response.StartChallengeRes;
import com.ssafy.Dito.domain.groups.service.GroupChallengeService;
import com.ssafy.Dito.domain.screentime.dto.response.GroupRankingRes;
import com.ssafy.Dito.domain.screentime.service.ScreenTimeService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import com.ssafy.Dito.global.dto.SingleResult;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Group Challenge", description = "그룹 챌린지 API")
@RestController
@RequestMapping("/challenges/groups")
@RequiredArgsConstructor
public class GroupChallengeController {

    private final GroupChallengeService groupChallengeService;
    private final ScreenTimeService screenTimeService;

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
            description = "잘못된 요청 (유효성 검증 실패 또는 코인 부족)",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "error": true,
                      "message": "코인이 부족합니다. 필요: 150, 보유: 100"
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
        @Valid @RequestBody CreateGroupChallengeReq request
    ) {
        Long userId = JwtAuthentication.getUserId();
        GroupChallengeRes response = groupChallengeService.createGroupChallenge(request, userId);
        return ApiResponse.create(response);
    }

    @Operation(
        summary = "그룹 챌린지 참여",
        description = "초대 코드를 사용하여 그룹 챌린지에 참여합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "그룹 참여 성공",
            content = @Content(schema = @Schema(implementation = JoinGroupRes.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (코인 부족)",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "error": true,
                      "message": "코인이 부족합니다. 필요: 150, 보유: 100"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "유효하지 않은 초대코드",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "error": true,
                      "message": "유효하지 않은 초대코드입니다"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이미 참여한 그룹",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "error": true,
                      "message": "이미 참여한 그룹입니다"
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/join")
    public ResponseEntity<SingleResult<JoinGroupRes>> joinGroup(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "그룹 참여 요청",
            required = true,
            content = @Content(schema = @Schema(implementation = JoinGroupReq.class))
        )
        @Valid @RequestBody JoinGroupReq request
    ) {
        Long userId = JwtAuthentication.getUserId();
        JoinGroupRes response = groupChallengeService.joinGroup(request, userId);
        return ApiResponse.of(HttpStatus.OK, "성공적으로 그룹에 참여했습니다!", response);
    }
    @Operation(
        summary = "참가자 생성"
    )

    @PutMapping("/create/participant")
    public ResponseEntity<CommonResult> createGroupParticipant(
        @RequestBody GroupParticipantReq req
    ) {
        groupChallengeService.createGroupParticipant(req);
        return ApiResponse.of(HttpStatus.OK, "그룹 참가자 생성 성공");
    }

    @Operation(
        summary = "그룹 챌린지 시작",
        description = "그룹 챌린지를 시작합니다. 호스트만 시작할 수 있습니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "챌린지 시작 성공",
            content = @Content(schema = @Schema(implementation = StartChallengeRes.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "이미 시작된 챌린지",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "error": true,
                      "message": "이미 시작된 챌린지입니다"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "권한 없음 (호스트가 아님)",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "error": true,
                      "message": "챌린지를 시작할 권한이 없습니다. 호스트만 시작할 수 있습니다"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "그룹을 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "error": true,
                      "message": "그룹 챌린지를 찾을 수 없습니다"
                    }
                    """
                )
            )
        )
    })
    @PutMapping("/{group_id}/start")
    public ResponseEntity<SingleResult<StartChallengeRes>> startChallenge(
        @io.swagger.v3.oas.annotations.Parameter(
            description = "그룹 챌린지 ID",
            required = true,
            example = "1"
        )
        @PathVariable("group_id") Long groupId
    ) {
        Long userId = JwtAuthentication.getUserId();
        StartChallengeRes response = groupChallengeService.startChallenge(groupId, userId);
        return ApiResponse.of(HttpStatus.OK, "챌린지가 성공적으로 시작되었습니다!", response);
    }

    @Operation(
        summary = "그룹 챌린지 참여자 목록 조회",
        description = "그룹 챌린지의 참여자 목록과 각 참여자가 장착한 아이템을 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "참여자 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = GroupParticipantsRes.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "그룹을 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "error": true,
                      "message": "그룹 챌린지를 찾을 수 없습니다"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/{group_id}/participants")
    public ResponseEntity<SingleResult<GroupParticipantsRes>> getParticipants(
        @io.swagger.v3.oas.annotations.Parameter(
            description = "그룹 챌린지 ID",
            required = true,
            example = "1"
        )
        @PathVariable("group_id") Long groupId
    ) {
        GroupParticipantsRes response = groupChallengeService.getParticipants(groupId);
        return ApiResponse.of(HttpStatus.OK, "참여자 목록 조회 성공", response);
    }

    @Operation(
        summary = "그룹 챌린지 랭킹 조회",
        description = "그룹 챌린지의 스크린타임 기반 랭킹을 조회합니다.\n\n" +
            "- 챌린지 기간 내 스크린타임 총합 기준\n" +
            "- 스크린타임이 적은 순으로 정렬 (1위 = 가장 적게 사용)\n" +
            "- 일평균 스크린타임 포함"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "랭킹 조회 성공",
            content = @Content(schema = @Schema(implementation = GroupRankingRes.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "그룹을 찾을 수 없음",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                      "error": true,
                      "message": "그룹 챌린지를 찾을 수 없습니다"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/{group_id}/ranking")
    public ResponseEntity<SingleResult<GroupRankingRes>> getGroupRanking(
        @io.swagger.v3.oas.annotations.Parameter(
            description = "그룹 챌린지 ID",
            required = true,
            example = "1"
        )
        @PathVariable("group_id") Long groupId
    ) {
        Long currentUserId = JwtAuthentication.getUserId();
        GroupRankingRes response = screenTimeService.getGroupRanking(groupId, currentUserId);
        return ApiResponse.of(HttpStatus.OK, "랭킹 조회 성공", response);
    }

    @Operation(summary = "로그인한 유저가 참여한 그룹 상세 조회")
    @GetMapping("/detail")
    public ResponseEntity<SingleResult<GroupDetailRes>> getGroupDetail() {
        GroupDetailRes res = groupChallengeService.getGroupDetail();
        return ApiResponse.ok(res);
    }

    @Operation(
        summary = "콕콕찌르기",
        description = "그룹 챌린지 참여자를 콕콕 찌릅니다. 대상 사용자에게 FCM 푸시 알림이 전송됩니다.\n\n" +
            "- 그룹 랭킹 화면에서 상대방 캐릭터 클릭 시 호출\n" +
            "- FCM 푸시 알림으로 \"[닉네임]님이 콕 찌르기를 보냈습니다!\" 메시지 전송"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "콕콕찌르기 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (자기 자신을 찌르기 등)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "그룹 또는 사용자를 찾을 수 없음"
        )
    })
    @PostMapping("/{group_id}/poke")
    public ResponseEntity<CommonResult> pokeParticipant(
        @io.swagger.v3.oas.annotations.Parameter(
            description = "그룹 챌린지 ID",
            required = true,
            example = "1"
        )
        @PathVariable("group_id") long groupId,

        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "콕콕찌르기 요청",
            required = true,
            content = @Content(schema = @Schema(implementation = PokeReq.class))
        )
        @Valid @RequestBody PokeReq request
    ) {
        groupChallengeService.pokeParticipant(groupId, request);
        return ApiResponse.of(HttpStatus.OK, "콕콕찌르기 성공");
    }
}
