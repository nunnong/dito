package com.ssafy.Dito.domain.groups.controller;

import com.ssafy.Dito.domain.groups.dto.request.CreateGroupChallengeRequest;
import com.ssafy.Dito.domain.groups.dto.response.GroupChallengeResponse;
import com.ssafy.Dito.domain.groups.service.GroupChallengeService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.SingleResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/challenges/groups")
@RequiredArgsConstructor
public class GroupChallengeController {

    private final GroupChallengeService groupChallengeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResult<GroupChallengeResponse> createGroupChallenge(
        @Valid @RequestBody CreateGroupChallengeRequest request,
        @RequestHeader("X-User-Id") Long userId // TODO: JWT 인증 구현 후 @AuthenticationPrincipal로 변경
    ) {
        GroupChallengeResponse response = groupChallengeService.createGroupChallenge(request, userId);
        return ApiResponse.ok(response);
    }
}
