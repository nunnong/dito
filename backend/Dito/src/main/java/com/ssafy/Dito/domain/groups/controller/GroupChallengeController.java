package com.ssafy.Dito.domain.groups.controller;

import com.ssafy.Dito.domain.groups.dto.request.CreateGroupChallengeReq;
import com.ssafy.Dito.domain.groups.dto.response.GroupChallengeRes;
import com.ssafy.Dito.domain.groups.service.GroupChallengeService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.SingleResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/challenges/groups")
@RequiredArgsConstructor
public class GroupChallengeController {

    private final GroupChallengeService groupChallengeService;

    @PostMapping
    public ResponseEntity<SingleResult<GroupChallengeRes>> createGroupChallenge(
        @Valid @RequestBody CreateGroupChallengeReq request,
        @RequestHeader("X-User-Id") Long userId // TODO: JWT 인증 구현 후 @AuthenticationPrincipal로 변경
    ) {
        GroupChallengeRes response = groupChallengeService.createGroupChallenge(request, userId);
        return ApiResponse.create(response);
    }
}
