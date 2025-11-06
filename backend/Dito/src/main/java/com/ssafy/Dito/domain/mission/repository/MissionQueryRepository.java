package com.ssafy.Dito.domain.mission.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.Dito.domain.mission.dto.request.AiMissionReq;
import com.ssafy.Dito.domain.mission.dto.request.MissionReq;
import com.ssafy.Dito.domain.mission.dto.response.AiMissionRes;
import com.ssafy.Dito.domain.mission.dto.response.MissionRes;
import com.ssafy.Dito.domain.mission.dto.response.QAiMissionRes;
import com.ssafy.Dito.domain.mission.dto.response.QMissionRes;
import com.ssafy.Dito.domain.mission.entity.QMission;
import com.ssafy.Dito.global.util.PageUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private final int PAGE_SIZE = 10;
    private final QMission mission = QMission.mission;

    public Page<MissionRes> getMissionPage(long pageNum) {

        Pageable pageRequest = PageRequest.of((int) pageNum, PAGE_SIZE);

        JPAQuery<Long> countQuery = jpaQueryFactory
                .from(mission)
                .select(mission.countDistinct());

        List<MissionRes> res = jpaQueryFactory
                .select(new QMissionRes(
                        mission.id,
                        mission.missionType,
                        mission.missionText,
                        mission.coinReward,
                        mission.status
                ))
                .from(mission)
                .offset(pageNum * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .orderBy(mission.id.desc())
                .fetch();

        return PageUtils.of(res, pageRequest, countQuery.fetchOne());
    }

    public List<AiMissionRes> getAiMissionRes(AiMissionReq req) {
        return jpaQueryFactory
            .select(Projections.constructor(
                AiMissionRes.class,
                mission.id,
                mission.missionType,
                mission.missionText,
                mission.coinReward,
                mission.triggerTime,
                mission.durationSeconds,
                mission.targetApp,
                mission.statChangeSelfCare,
                mission.statChangeFocus,
                mission.statChangeSleep,
                mission.status,
                mission.prompt,
                mission.user
            ))
            .from(mission)
            .where(mission.user.id.eq(req.userId()))
            .fetch();
    }
}
