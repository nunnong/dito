package com.ssafy.Dito.domain.missionResult.entity;

import com.ssafy.Dito.domain._common.IdentifiableEntity;
import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.missionResult.dto.request.MissionResultReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.sql.Timestamp;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionResult extends IdentifiableEntity {

    @Column(name = "result", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("미션 수행 결과")
    private Result result;

    @Column(name ="completed_at", nullable = true)
    @CreatedDate
    @Comment("미션 완료일")
    private Timestamp completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    @Comment("미션")
    private Mission mission;

    private MissionResult(Result result, Mission mission) {
        this.result = result;
        this.mission = mission;
    }

    public static MissionResult of(MissionResultReq req, Mission mission) {
        return new MissionResult(
            req.result(),
            mission
        );
    }
}