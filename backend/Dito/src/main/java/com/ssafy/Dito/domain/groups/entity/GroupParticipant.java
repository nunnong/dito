package com.ssafy.Dito.domain.groups.entity;

import com.ssafy.Dito.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "group_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("그룹 참여자")
public class GroupParticipant {

    @EmbeddedId
    private GroupParticipantId id;

    @Column(name = "role", length = 10, nullable = false)
    @Comment("역할 (host, guest)")
    private String role;

    @Column(name = "bet_coins", nullable = false)
    @Comment("베팅 코인")
    private int betCoins;

    @Column(name = "rank", nullable = false)
    @Comment("순위")
    private int rank;

    @Column(name = "avg_screen_time", precision = 5, scale = 2, nullable = false)
    @Comment("평균 스크린 타임")
    private BigDecimal avgScreenTime;

    @Column(name = "joined_at", nullable = false)
    @Comment("참여 일시")
    private Instant joinedAt;

    private GroupParticipant(GroupParticipantId id, String role, int betCoins, int rank,
        BigDecimal avgScreenTime, Instant joinedAt) {
        this.id = id;
        this.role = role;
        this.betCoins = betCoins;
        this.rank = rank;
        this.avgScreenTime = avgScreenTime;
        this.joinedAt = joinedAt;
    }

    public static GroupParticipant ofHost(User user, GroupChallenge group, int betCoins) {
        return new GroupParticipant(
            new GroupParticipantId(user, group),
            "host",
            betCoins,
            0,
            BigDecimal.ZERO,
            Instant.now()
        );
    }

    public static GroupParticipant ofGuest(User user, GroupChallenge group, int betCoins) {
        return new GroupParticipant(
            new GroupParticipantId(user, group),
            "guest",
            betCoins,
            0,
            BigDecimal.ZERO,
            Instant.now()
        );
    }

    @Embeddable
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @EqualsAndHashCode
    public static class GroupParticipantId implements Serializable {

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        @Comment("사용자")
        private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "group_id", nullable = false)
        @Comment("그룹")
        private GroupChallenge group;
    }
}
