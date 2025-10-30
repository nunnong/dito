package com.ssafy.Dito.domain.groups.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "\"group\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("그룹 챌린지 식별자")
    private Long id;

    @Column(name = "group_name", length = 100, nullable = false)
    @Comment("그룹 챌린지 이름")
    private String groupName;

    @Column(name = "invite_code", length = 4, nullable = false, unique = true)
    @Comment("초대 코드")
    private String inviteCode;

    @Column(name = "period", nullable = false)
    @Comment("챌린지 기간 (일)")
    private int period;

    @Column(name = "start_date", nullable = true)
    @Comment("시작 일자")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = true)
    @Comment("종료 일자")
    private LocalDate endDate;

    @Column(name = "goal_description", columnDefinition = "TEXT", nullable = true)
    @Comment("목표 설명")
    private String goalDescription;

    @Column(name = "penalty_description", columnDefinition = "TEXT", nullable = true)
    @Comment("페널티 설명")
    private String penaltyDescription;

    @Column(name = "status", length = 20, nullable = false)
    @ColumnDefault("'pending'")
    @Comment("챌린지 상태 (pending, in_progress, completed, cancelled)")
    private String status;

    @Column(name = "total_bet_coins", nullable = false)
    @Comment("총 베팅 코인")
    private int totalBetCoins;

    @Column(name = "created_at", nullable = false)
    @Comment("생성 일시")
    private Instant createdAt;

    private GroupChallenge(String groupName, String inviteCode, int period, LocalDate startDate,
        LocalDate endDate, String goalDescription, String penaltyDescription, String status,
        int totalBetCoins, Instant createdAt) {
        this.groupName = groupName;
        this.inviteCode = inviteCode;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.goalDescription = goalDescription;
        this.penaltyDescription = penaltyDescription;
        this.status = status;
        this.totalBetCoins = totalBetCoins;
        this.createdAt = createdAt;
    }

    public static GroupChallenge of(String groupName, String inviteCode, int period,
        String goalDescription, String penaltyDescription, int betCoins) {
        return new GroupChallenge(groupName, inviteCode, period, null, null, goalDescription,
            penaltyDescription, "pending", betCoins, Instant.now());
    }
}
