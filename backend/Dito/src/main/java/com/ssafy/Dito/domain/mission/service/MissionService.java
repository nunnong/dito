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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

        // 가장 최근 진행 중인 미션이 있는지 확인 (trigger_time 기준 내림차순)
        Optional<Mission> existingMission = missionRepository.findFirstByUser_IdAndStatusOrderByTriggerTimeDesc(userId, Status.IN_PROGRESS);

        if (existingMission.isPresent()) {
            Mission mission = existingMission.get();
            // trigger_time + duration_seconds로 만료 시간 계산
            Instant expirationTime = mission.getTriggerTime().toInstant()
                .plusSeconds(mission.getDurationSeconds());

            // 현재 시간이 만료 시간보다 이전이면 (아직 유효한 미션이면) 예외 발생
            if (Instant.now().isBefore(expirationTime)) {
                throw new BadRequestException("이미 진행 중인 미션이 있습니다");
            }
            // 만료된 미션이면 새 미션 생성 허용 (아래 로직 계속 진행)
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