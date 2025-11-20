package com.ssafy.Dito.domain.ai.evaluation.controller;

import com.ssafy.Dito.domain.ai.evaluation.dto.EvaluationRequest;
import com.ssafy.Dito.domain.ai.evaluation.dto.EvaluationResponse;
import com.ssafy.Dito.domain.ai.evaluation.service.EvaluationService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import com.ssafy.Dito.global.dto.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI Evaluation Controller
 * Handles mission evaluation API requests
 *
 * Endpoint: POST /ai/evaluation
 * - Evaluates mission performance based on behavior logs
 * - Integrates with external AI server for evaluation logic
 * - Updates mission status and creates mission results
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "에이전트서버로 보냄", description = "AI 에이전트 서버 통신 API")
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * POST /ai/evaluation
     * Evaluate mission performance
     *
     * @param request EvaluationRequest containing mission and behavior data
     * @return EvaluationResponse with evaluation results
     */
    @PostMapping("/evaluation")
    @Operation(
            summary = "미션 평가 요청",
            description = "사용자의 미션 수행 결과를 AI 기반으로 평가합니다. " +
                    "요청 즉시 pending 상태로 응답하며, 실제 평가는 비동기로 처리됩니다. " +
                    "behavior_logs는 빈 배열([]) 또는 null 허용 - AI가 의미를 판단합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "평가 요청 접수 성공",
                    content = @Content(
                            schema = @Schema(implementation = EvaluationResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "run_id": "eval_550e8400-e29b-41d4-a716-446655440001",
                                      "thread_id": "thread_660e8400-e29b-41d4-a716-446655440000",
                                      "status": "pending"
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 필드 누락, 잘못된 데이터 형식, 이미 완료된 미션 등)",
                    content = @Content(
                            schema = @Schema(implementation = CommonResult.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "error": true,
                                      "message": "이미 완료된 미션입니다: mission_20251105_001"
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 또는 미션을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = CommonResult.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "error": true,
                                      "message": "해당 사용자를 찾을 수 없습니다"
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "502",
                    description = "AI 서버 오류",
                    content = @Content(
                            schema = @Schema(implementation = CommonResult.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "error": true,
                                      "message": "AI 서버 호출 실패: Connection timeout"
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "504",
                    description = "AI 서버 타임아웃",
                    content = @Content(
                            schema = @Schema(implementation = CommonResult.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "error": true,
                                      "message": "AI 서버 응답 시간 초과"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<SingleResult<EvaluationResponse>> evaluateMission(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "미션 평가 요청 데이터 (behavior_logs는 빈 배열 또는 null 허용)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EvaluationRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "로그 있음",
                                            description = "행동 로그가 있는 경우",
                                            value = """
                                            {
                                              "user_id": "user_12345",
                                              "mission_id": "mission_20251105_001",
                                              "mission_info": {
                                                "type": "REST",
                                                "instruction": "5분간 휴식하세요",
                                                "duration_seconds": 300,
                                                "target_apps": ["com.google.android.youtube", "com.instagram.android"],
                                                "start_time": "2025-11-05T14:30:00+09:00",
                                                "end_time": "2025-11-05T14:35:00+09:00"
                                              },
                                              "behavior_logs": [
                                                {
                                                  "log_type": "APP_USAGE",
                                                  "sequence": 1,
                                                  "timestamp": "2025-11-05T14:30:15+09:00",
                                                  "package_name": "com.instagram.android",
                                                  "app_name": "Instagram",
                                                  "duration_seconds": 125,
                                                  "is_target_app": true
                                                },
                                                {
                                                  "log_type": "SCREEN_OFF",
                                                  "sequence": 2,
                                                  "timestamp": "2025-11-05T14:33:30+09:00"
                                                }
                                              ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "로그 없음",
                                            description = "행동 로그가 없는 경우 (빈 배열)",
                                            value = """
                                            {
                                              "user_id": "user_12345",
                                              "mission_id": "mission_20251105_002",
                                              "mission_info": {
                                                "type": "REST",
                                                "instruction": "5분간 휴식하세요",
                                                "duration_seconds": 300,
                                                "target_apps": ["com.google.android.youtube", "com.instagram.android"],
                                                "start_time": "2025-11-05T15:00:00+09:00",
                                                "end_time": "2025-11-05T15:05:00+09:00"
                                              },
                                              "behavior_logs": []
                                            }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody EvaluationRequest request
    ) {
        log.info("Received evaluation request - userId: {}, missionId: {}",
                request.userId(), request.missionId());

        EvaluationResponse response = evaluationService.evaluateMission(request);

        log.info("Evaluation request accepted - runId: {}, threadId: {}, status: {}",
                response.runId(),
                response.threadId(),
                response.status());

        return ApiResponse.ok(response);
    }
}
