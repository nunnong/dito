package com.ssafy.Dito.domain.mission.entity;

import com.ssafy.Dito.domain._common.IdentifiableEntity;
import com.ssafy.Dito.domain.mission.dto.request.MissionReq;
import com.ssafy.Dito.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class Mission extends IdentifiableEntity {

    @Column(name = "mission_type", length = 20, nullable = false)
    @Comment("미션 타입")
    private String missionType;

    @Column(name = "mission_text", length = 255, nullable = false)
    @Comment("미션 내용")
    private String missionText;

    @Column(name = "coin_reward", nullable = false)
    @Comment("미션 코인 보상")
    private int coinReward;

    @Column(name = "trigger_time", nullable = true)
    @CreatedDate
    @Comment("미션 시작 시간")
    private Timestamp triggerTime;

    @Column(name = "duration_seconds", nullable = false)
    @Comment("미션 지속 시간")
    private int durationSeconds;

    @Column(name = "target_app", length = 100, nullable = false)
    @Comment("타겟 앱")
    private String targetApp;

    @Column(name = "stat_change_self_care", nullable = false)
    @Comment("자기관리 능력치 변화")
    private int statChangeSelfCare;

    @Column(name = "stat_change_focus", nullable = false)
    @Comment("집중도 능력치 변화")
    private int statChangeFocus;

    @Column(name = "stat_change_sleep", nullable = false)
    @Comment("수면 능력치 변화")
    private int statChangeSleep;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("미션 진행 상태")
    private Status status;

    @Column(name = "prompt", nullable = false)
    @Comment("프롬프트")
    private String prompt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Mission(String missionType, String missionText, int coinReward,
                    int durationSeconds, String targetApp, int statChangeSelfCare, int statChangeFocus,
                    int statChangeSleep, String prompt, User user) {
        this.missionType = missionType;
        this.missionText = missionText;
        this.coinReward = coinReward;
        this.triggerTime = null;
        this.durationSeconds = durationSeconds;
        this.targetApp = targetApp;
        this.statChangeSelfCare = statChangeSelfCare;
        this.statChangeFocus = statChangeFocus;
        this.statChangeSleep = statChangeSleep;
        this.status = Status.IN_PROGRESS;
        this.prompt = prompt;
        this.user = user;
    }

    public static Mission of(MissionReq req, User user){
        return new Mission(
                req.missionType(),
                req.missionText(),
                req.coinReward(),
                req.durationSeconds(),
                req.targetApp(),
                req.statChangeSelfCare(),
                req.statChangeFocus(),
                req.statChangeSleep(),
                req.prompt(),
                user
        );
    }

    public void updateStatus() {
        this.status = Status.COMPLETED;
    }
}
