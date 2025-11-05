package com.ssafy.Dito.domain.missionResult.mapper;

import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.missionResult.dto.request.MissionResultReq;
import com.ssafy.Dito.domain.missionResult.entity.Result;
import com.ssafy.Dito.domain.status.entity.Status;
import com.ssafy.Dito.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionResultMapper {

    public void updateUserInfo(MissionResultReq req, Mission mission, User user, Status status){

        int selfCareDelta = calculateDelta(mission.getStatChangeSelfCare(), req.result());
        int focusDelta = calculateDelta(mission.getStatChangeFocus(), req.result());
        int sleepDelta = calculateDelta(mission.getStatChangeSleep(), req.result());

        if (req.result() == Result.SUCCESS) {
            user.updateCoin(mission.getCoinReward());
        }

        status.updateUserStatus(selfCareDelta, focusDelta, sleepDelta);
    }

    private int calculateDelta(int basePoint, Result result) {
        return (result == Result.SUCCESS) ? basePoint : -basePoint;
    }
}