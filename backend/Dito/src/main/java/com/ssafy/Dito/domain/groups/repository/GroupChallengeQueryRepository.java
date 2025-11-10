package com.ssafy.Dito.domain.groups.repository;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.Dito.domain.groups.dto.response.GroupDetailRes;
import com.ssafy.Dito.domain.groups.dto.response.QGroupDetailRes;
import com.ssafy.Dito.domain.groups.entity.QGroupChallenge;
import com.ssafy.Dito.domain.groups.entity.QGroupParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupChallengeQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private final QGroupChallenge groupChallenge = QGroupChallenge.groupChallenge;
    private final QGroupParticipant groupParticipant = QGroupParticipant.groupParticipant;

    public GroupDetailRes getGroupDetail(long userId) {
        return jpaQueryFactory
            .select(new QGroupDetailRes(
                groupChallenge.id,
                groupChallenge.groupName,
                groupChallenge.goalDescription,
                groupChallenge.penaltyDescription,
                groupChallenge.period,
                groupParticipant.betCoins,
                groupChallenge.totalBetCoins,
                groupChallenge.inviteCode,
                groupChallenge.startDate,
                groupChallenge.endDate,
                groupChallenge.status,
                new CaseBuilder()
                    .when(groupParticipant.role.eq("host")).then(true)
                    .otherwise(false)
            ))
            .from(groupParticipant)
            .join(groupParticipant.id.group, groupChallenge)
            .where(
                groupParticipant.id.user.id.eq(userId),
                groupChallenge.status.in("pending", "in_progress")  // 활성 상태의 그룹만
            )
            .orderBy(groupChallenge.createdAt.desc())  // 최신 그룹 우선
            .fetchFirst();  // 첫 번째 결과만
    }
}
