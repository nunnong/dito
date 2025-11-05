package com.ssafy.Dito.domain.weaklyGoal.entity;

import com.ssafy.Dito.domain._common.IdentifiableEntity;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.weaklyGoal.dto.request.WeeklyGoalReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
public class WeeklyGoal extends IdentifiableEntity {

    @Column(name =  "goal", length = 255 , nullable = false)
    @Comment("주간 목표")
    private String goal;

    @Column(name = "start_at", nullable = false)
    @CreatedDate
    @Comment("주간 목표 시작일")
    private Timestamp startAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("유저 식별자")
    private User user;

    private WeeklyGoal(String goal, User user) {
        this.goal = goal;
        this.user = user;
    }

    public static WeeklyGoal of(WeeklyGoalReq req, User user) {
        return new WeeklyGoal(
                req.goal(),
                user
        );
    }
}
