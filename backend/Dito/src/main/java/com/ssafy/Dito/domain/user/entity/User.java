package com.ssafy.Dito.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("유저 식별자")
    private Long id;

    @Column(name="personal_id", length = 255, nullable = false)
    @Comment("개인 아이디")
    private String personalId;

    @Column(name="password", length = 50, nullable = false)
    @Comment("비밀번호")
    private String password;

    @Column(name = "nickname", length = 50, nullable = false)
    @Comment("닉네임")
    private String nickname;

    @Column(name = "birth", nullable = false)
    @Comment("나이")
    private Date birth;

    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("성별")
    private Gender gender;

    @Column(name = "job", nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ETC'")
    @Comment("직업")
    private Job job;

    @Column(name = "coin_balance", nullable = false)
    @Comment("코인 잔액")
    private int coinBalance;

    @Column(name = "frequency", nullable = false)
    @ColumnDefault("'NORMAL'")
    @Comment("빈도")
    private Frequency frequency;

    @Column(name = "last_login_at", nullable = true)
    @Comment("마지막 로그인")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false)
    @Comment("가입일")
    private Instant createdAt;

    @Column(name = "FCM_token", length = 255, nullable = false)
    @Comment("FCM 토큰")
    private String fcmToken;

    private User(String personalId, String password, String nickname, Date birth, Gender gender,
        Job job, int coinBalance, Instant lastLoginAt, Instant createdAt, String fcmToken) {
        this.personalId = personalId;
        this.password = password;
        this.nickname = nickname;
        this.birth = birth;
        this.gender = gender;
        this.job = job;
        this.coinBalance = coinBalance;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.fcmToken = fcmToken;
    }

    public static User of(String personalId, String password, String nickname, Date birth, Gender gender,
        Job job, int coinBalance, Instant lastLoginAt, Instant createdAt, String fcmToken) {
        return new User(personalId, password, nickname, birth, gender, job, coinBalance, lastLoginAt, createdAt, fcmToken);
    }
}