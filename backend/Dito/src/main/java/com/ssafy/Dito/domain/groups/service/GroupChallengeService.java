package com.ssafy.Dito.domain.groups.service;

import com.ssafy.Dito.domain.groups.dto.request.CreateGroupChallengeReq;
import com.ssafy.Dito.domain.groups.dto.request.GroupParticipantReq;
import com.ssafy.Dito.domain.groups.dto.request.JoinGroupReq;
import com.ssafy.Dito.domain.groups.dto.response.GroupChallengeRes;
import com.ssafy.Dito.domain.groups.dto.response.GroupParticipantsRes;
import com.ssafy.Dito.domain.groups.dto.response.JoinGroupRes;
import com.ssafy.Dito.domain.groups.dto.response.StartChallengeRes;
import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import com.ssafy.Dito.domain.groups.entity.GroupParticipant;
import com.ssafy.Dito.domain.groups.exception.AlreadyJoinedGroupException;
import com.ssafy.Dito.domain.groups.exception.ChallengeAlreadyStartedException;
import com.ssafy.Dito.domain.groups.exception.GroupNotFoundException;
import com.ssafy.Dito.domain.groups.exception.InsufficientCoinsException;
import com.ssafy.Dito.domain.groups.exception.InvalidInviteCodeException;
import com.ssafy.Dito.domain.groups.exception.UnauthorizedStartChallengeException;
import com.ssafy.Dito.domain.groups.repository.GroupChallengeRepository;
import com.ssafy.Dito.domain.groups.repository.GroupParticipantQueryRepository;
import com.ssafy.Dito.domain.groups.repository.GroupParticipantRepository;
import com.ssafy.Dito.domain.groups.util.InviteCodeGenerator;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupChallengeService {

    private final GroupChallengeRepository groupChallengeRepository;
    private final GroupParticipantRepository groupParticipantRepository;
    private final GroupParticipantQueryRepository groupParticipantQueryRepository;
    private final UserRepository userRepository;
    private static final int MAX_INVITE_CODE_ATTEMPTS = 10;

    @Transactional
    public GroupChallengeRes createGroupChallenge(CreateGroupChallengeReq request, Long hostUserId) {
        String inviteCode = generateUniqueInviteCode();

        // 호스트 조회 및 코인 차감
        User host = userRepository.findById(hostUserId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        if (host.getCoinBalance() < request.betCoins()) {
            throw new InsufficientCoinsException(request.betCoins(), host.getCoinBalance());
        }
        host.deductCoins(request.betCoins());

        GroupChallenge groupChallenge = GroupChallenge.of(
            request.groupName(),
            inviteCode,
            request.period(),
            request.goalDescription(),
            request.penaltyDescription(),
            request.betCoins()
        );

        GroupChallenge savedChallenge = groupChallengeRepository.save(groupChallenge);

        // 호스트를 group_participant에 추가
        GroupParticipant hostParticipant = GroupParticipant.ofHost(
            host,
            savedChallenge,
            request.betCoins()
        );
        groupParticipantRepository.save(hostParticipant);

        return GroupChallengeRes.from(savedChallenge, hostUserId, request.betCoins());
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

        JoinGroupRes res = groupChallenge.of();

//        // 4. 코인 충분한지 확인 및 차감
//        if (user.getCoinBalance() < request.betCoins()) {
//            throw new InsufficientCoinsException(request.betCoins(), user.getCoinBalance());
//        }
//        user.deductCoins(request.betCoins());
//
//        // 5. 그룹의 총 베팅 코인 증가
//        groupChallenge.addBetCoins(request.betCoins());
//
//        // 6. 참여자 추가 (role: guest)
//        GroupParticipant participant = GroupParticipant.ofGuest(
//            user,
//            groupChallenge,
//            request.betCoins()
//        );
//        groupParticipantRepository.save(participant);

        // 7. 응답 생성
        return res;
    }

    @Transactional
    public StartChallengeRes startChallenge(Long groupId, Long userId) {
        // 1. 그룹 챌린지 조회
        GroupChallenge groupChallenge = groupChallengeRepository.findById(groupId)
            .orElseThrow(GroupNotFoundException::new);

        // 2. 이미 시작된 챌린지인지 확인
        if (!"pending".equals(groupChallenge.getStatus())) {
            throw new ChallengeAlreadyStartedException();
        }

        // 3. 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        // 4. 사용자가 해당 그룹의 호스트인지 확인
        GroupParticipant participant = groupParticipantRepository.findByIdUserAndIdGroup(user, groupChallenge)
            .orElseThrow(() -> new RuntimeException("그룹에 참여하지 않은 사용자입니다"));

        if (!"host".equals(participant.getRole())) {
            throw new UnauthorizedStartChallengeException();
        }

        // 5. 챌린지 시작
        groupChallenge.startChallenge();

        // 6. 응답 생성
        return StartChallengeRes.from(groupChallenge);
    }

    @Transactional(readOnly = true)
    public GroupParticipantsRes getParticipants(Long groupId) {
        // 그룹 존재 여부 확인
        groupChallengeRepository.findById(groupId)
            .orElseThrow(GroupNotFoundException::new);

        return groupParticipantQueryRepository.getParticipants(groupId);
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

    @Transactional
    public void createGroupPariticipant(GroupParticipantReq req) {
        long userId = JwtAuthentication.getUserId();
        User user = userRepository.getById(userId);

        GroupChallenge groupChallenge = groupChallengeRepository.getById(req.groupId());

        if (user.getCoinBalance() < req.betCoin()) {
            throw new InsufficientCoinsException(req.betCoin(), user.getCoinBalance());
        }
        user.deductCoins(req.betCoin());

        groupChallenge.addBetCoins(req.betCoin());

        GroupParticipant participant = GroupParticipant.ofGuest(
                user,
                groupChallenge,
                req.betCoin()
        );

        groupParticipantRepository.save(participant);
    }
}