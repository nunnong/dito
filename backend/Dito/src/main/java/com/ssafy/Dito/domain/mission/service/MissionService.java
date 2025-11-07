package com.ssafy.Dito.domain.mission.service;

import com.ssafy.Dito.domain.ai.api.dto.AiReq;
import com.ssafy.Dito.domain.mission.dto.request.AiMissionReq;
import com.ssafy.Dito.domain.mission.dto.request.MissionReq;
import com.ssafy.Dito.domain.mission.dto.response.AiMissionRes;
import com.ssafy.Dito.domain.mission.dto.response.MissionRes;
import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.mission.entity.Status;
import com.ssafy.Dito.domain.mission.repository.MissionQueryRepository;
import com.ssafy.Dito.domain.mission.repository.MissionRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.global.exception.BadRequestException;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissionService {
    private final MissionQueryRepository missionQueryRepository;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;

    public Page<MissionRes> getMissions(long page_number) {
        return missionQueryRepository.getMissionPage(page_number);
    }

    public Long createMission(MissionReq req) {
        long userId = req.userId();

        // 진행 중인 미션이 이미 있는지 확인
        if (missionRepository.existsByUser_IdAndStatus(userId, Status.IN_PROGRESS)) {
            throw new BadRequestException("이미 진행 중인 미션이 있습니다");
        }

        User user = userRepository.getById(userId);

        Mission mission = Mission.of(req, user);
        Mission savedMission = missionRepository.save(mission);
        return savedMission.getId();
    }

    public List<AiMissionRes> getMissionForAi(AiReq req) {
        return missionQueryRepository.getAiMissionRes(req);
    }
}