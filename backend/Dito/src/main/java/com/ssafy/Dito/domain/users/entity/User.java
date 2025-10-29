package com.ssafy.Dito.domain.users.entity;

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

    @Column(name="email", length = 255, nullable = false)
    @Comment("이메일")
    private String email;

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

    @Column(name = "job", length = 255, nullable = false)
    @ColumnDefault("기타")
    @Comment("직업")
    private String job;

    @Column(name = "coin_balance", nullable = false)
    @ColumnDefault("0")
    @Comment("코인 잔액")
    private int coinBalance;

    @Column(name = "last_login_at", nullable = true)
    @Comment("마지막 로그인")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false)
    @Comment("가입일")
    private Instant createdAt;

    @Column(name = "FCM_token", length = 255, nullable = false)
    @Comment("FCM 토큰")
    private String fcmToken;

    private User(String email, String password, String nickname, Date birth, Gender gender,
        String job, int coinBalance, Instant lastLoginAt, Instant createdAt, String fcmToken) {
        this.email = email;
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

    public static User of(String email, String password, String nickname, Date birth, Gender gender,
        String job, int coinBalance, Instant lastLoginAt, Instant createdAt, String fcmToken) {
        return new User(email, password, nickname, birth, gender, job, coinBalance, lastLoginAt, createdAt, fcmToken);
    }
}