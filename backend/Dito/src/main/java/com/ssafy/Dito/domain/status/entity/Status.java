package com.ssafy.Dito.domain.status.entity;

import com.ssafy.Dito.domain._common.IdentifiableEntity;
import com.ssafy.Dito.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Status extends IdentifiableEntity {

    @Column(name = "self_care_stat", nullable = false)
    @Comment("자기관리 스탯")
    private int selfCareStat;

    @Column(name = "focus_stat", nullable = false)
    @Comment("집중력 스탯")
    private int focusStat;

    @Column(name = "sleep_stat", nullable = false)
    @Comment("수면 스탯")
    private int sleepStat;

    @Column(name = "total_stat", nullable = false)
    @Comment("총점 스탯")
    private int totalStat;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("유저")
    private User user;

    private Status(User user) {
        this.selfCareStat = 50;
        this.focusStat = 50;
        this.sleepStat = 50;
        this.totalStat = 65;
        this.user = user;
    }

    public static Status of(User user) {
        return new Status(user);
    }

    public void updateUserStatus(int selfCare, int focus, int sleep) {
        this.selfCareStat = applyStatLimit(this.selfCareStat, selfCare);
        this.focusStat = applyStatLimit(this.focusStat, focus);
        this.sleepStat = applyStatLimit(this.sleepStat, sleep);

        this.totalStat = (selfCareStat + focusStat + sleepStat) / 3;
    }

    private int applyStatLimit(int current, int change) {
        return Math.max(0, Math.min(100, current + change));
    }
}