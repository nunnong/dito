package com.ssafy.Dito.domain.missionResult.service;

import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.mission.repository.MissionRepository;
import com.ssafy.Dito.domain.missionResult.dto.request.MissionResultReq;
import com.ssafy.Dito.domain.missionResult.entity.MissionResult;
import com.ssafy.Dito.domain.missionResult.entity.Result;
import com.ssafy.Dito.domain.missionResult.repository.MissionResultRepository;
import com.ssafy.Dito.domain.status.entity.Status;
import com.ssafy.Dito.domain.status.repository.StatusRepository;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionResultService {

    private final MissionResultRepository missionResultRepository;
    private final MissionRepository missionRepository;
    private final StatusRepository statusRepository;

    @Transactional
    public void createMissionResult(MissionResultReq req) {
        Mission mission = missionRepository.getById(req.missionId());
        MissionResult missionResult = MissionResult.of(req, mission);

        missionResultRepository.save(missionResult);

        long userId = mission.getUser().getId();
        Status status = statusRepository.getById(userId);

        int selfCarePoint = mission.getStatChangeSelfCare();
        int focusPoint = mission.getStatChangeFocus();
        int sleepPoint = mission.getStatChangeSleep();

        if(!req.result().equals((Result.SUCCESS))){
            selfCarePoint *= -1;
            focusPoint *= -1;
            sleepPoint *= -1;
        }

        status.updateUserStatus(selfCarePoint, focusPoint, sleepPoint);
    }
}
