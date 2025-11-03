package com.ssafy.Dito.domain.groups.service;

import com.ssafy.Dito.domain.groups.dto.request.CreateGroupChallengeReq;
import com.ssafy.Dito.domain.groups.dto.request.JoinGroupReq;
import com.ssafy.Dito.domain.groups.dto.response.GroupChallengeRes;
import com.ssafy.Dito.domain.groups.dto.response.JoinGroupRes;
import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import com.ssafy.Dito.domain.groups.entity.GroupParticipant;
import com.ssafy.Dito.domain.groups.exception.AlreadyJoinedGroupException;
import com.ssafy.Dito.domain.groups.exception.InsufficientCoinsException;
import com.ssafy.Dito.domain.groups.exception.InvalidInviteCodeException;
import com.ssafy.Dito.domain.groups.repository.GroupChallengeRepository;
import com.ssafy.Dito.domain.groups.repository.GroupParticipantRepository;
import com.ssafy.Dito.domain.groups.util.InviteCodeGenerator;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupChallengeService {

    private final GroupChallengeRepository groupChallengeRepository;
    private final GroupParticipantRepository groupParticipantRepository;
    private final UserRepository userRepository;
    private static final int MAX_INVITE_CODE_ATTEMPTS = 10;

    @Transactional
    public GroupChallengeRes createGroupChallenge(CreateGroupChallengeReq request, Long creatorUserId) {
        String inviteCode = generateUniqueInviteCode();

        // 생성자 조회 및 코인 차감
        User creator = userRepository.findById(creatorUserId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        if (creator.getCoinBalance() < request.betCoins()) {
            throw new InsufficientCoinsException(request.betCoins(), creator.getCoinBalance());
        }
        creator.deductCoins(request.betCoins());

        GroupChallenge groupChallenge = GroupChallenge.of(
            request.groupName(),
            inviteCode,
            request.period(),
            request.goalDescription(),
            request.penaltyDescription(),
            request.betCoins()
        );

        GroupChallenge savedChallenge = groupChallengeRepository.save(groupChallenge);

        // 생성자를 host로 group_participant에 추가
        GroupParticipant hostParticipant = GroupParticipant.ofHost(
            creator,
            savedChallenge,
            request.betCoins()
        );
        groupParticipantRepository.save(hostParticipant);

        return GroupChallengeRes.from(savedChallenge, creatorUserId);
    }

    @Transactional
    public JoinGroupRes joinGroup(JoinGroupReq request, Long userId) {
        // 1. 초대 코드로 그룹 조회
        GroupChallenge groupChallenge = groupChallengeRepository.findByInviteCode(request.inviteCode())
            .orElseThrow(InvalidInviteCodeException::new);

        // 2. 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        // 3. 이미 참여했는지 확인
        if (groupParticipantRepository.existsByIdUserAndIdGroup(user, groupChallenge)) {
            throw new AlreadyJoinedGroupException();
        }

        // 4. 코인 충분한지 확인 및 차감
        if (user.getCoinBalance() < request.betCoins()) {
            throw new InsufficientCoinsException(request.betCoins(), user.getCoinBalance());
        }
        user.deductCoins(request.betCoins());

        // 5. 그룹의 총 베팅 코인 증가
        groupChallenge.addBetCoins(request.betCoins());

        // 6. 참여자 추가 (role: guest)
        GroupParticipant participant = GroupParticipant.ofGuest(
            user,
            groupChallenge,
            request.betCoins()
        );
        groupParticipantRepository.save(participant);

        // 7. 응답 생성
        return JoinGroupRes.from(groupChallenge);
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
