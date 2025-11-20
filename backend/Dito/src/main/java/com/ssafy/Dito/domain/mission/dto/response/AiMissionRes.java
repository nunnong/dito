package com.ssafy.Dito.domain.mission.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.ssafy.Dito.domain.mission.entity.Status;
import com.ssafy.Dito.domain.user.entity.User;
import java.sql.Timestamp;

public record AiMissionRes (
    long id,
    String missionType,
    String missionText,
    int coinReward,
    Timestamp triggerTime,
    int durationSeconds,
    String targetApp,
    int statChangeSelfCare,
    int statChangeFocus,
    int statChangeSleep,
    Status status,
    String prompt,
    User user

){
    @QueryProjection
    public AiMissionRes {
    }
}
