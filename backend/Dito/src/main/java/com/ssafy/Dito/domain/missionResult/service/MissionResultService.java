package com.ssafy.Dito.domain.missionResult.service;

import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.mission.repository.MissionRepository;
import com.ssafy.Dito.domain.missionResult.dto.request.MissionResultReq;
import com.ssafy.Dito.domain.missionResult.entity.MissionResult;
import com.ssafy.Dito.domain.missionResult.repository.MissionResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissionResultService {

    private final MissionResultRepository missionResultRepository;
    private final MissionRepository missionRepository;

    public void createMissionResult(MissionResultReq req) {
        Mission mission = missionRepository.getById(req.missionId());
        MissionResult missionResult = MissionResult.of(req, mission);

        missionResultRepository.save(missionResult);
    }
}
