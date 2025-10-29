package com.ssafy.Dito.domain.groups.service;

import com.ssafy.Dito.domain.groups.dto.request.CreateGroupChallengeRequest;
import com.ssafy.Dito.domain.groups.dto.response.GroupChallengeResponse;
import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import com.ssafy.Dito.domain.groups.repository.GroupChallengeRepository;
import com.ssafy.Dito.domain.groups.util.InviteCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupChallengeService {

    private final GroupChallengeRepository groupChallengeRepository;
    private static final int MAX_INVITE_CODE_ATTEMPTS = 10;

    @Transactional
    public GroupChallengeResponse createGroupChallenge(CreateGroupChallengeRequest request, Long creatorUserId) {
        String inviteCode = generateUniqueInviteCode();

        GroupChallenge groupChallenge = GroupChallenge.of(
            request.groupName(),
            inviteCode,
            request.period(),
            request.goalDescription(),
            request.penaltyDescription(),
            request.betCoins()
        );

        GroupChallenge savedChallenge = groupChallengeRepository.save(groupChallenge);

        return GroupChallengeResponse.from(savedChallenge, creatorUserId);
    }

    private String generateUniqueInviteCode() {
        for (int attempt = 0; attempt < MAX_INVITE_CODE_ATTEMPTS; attempt++) {
            String inviteCode = InviteCodeGenerator.generate();
            if (!groupChallengeRepository.existsByInviteCode(inviteCode)) {
                return inviteCode;
            }
        }
        throw new RuntimeException("초대 코드 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }
}
