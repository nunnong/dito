package com.ssafy.Dito.domain.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import java.time.Instant;

@Entity
@Table(name = "\"user\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("유저 식별자")
    private Long id;

    @Column(name="personal_id", length = 255, nullable = false)
    @Comment("개인 ID")
    private String personalId;

    @Column(name="password", length = 50, nullable = false)
    @Comment("비밀번호")
    private String password;

    @Column(name = "nickname", length = 50, nullable = false)
    @Comment("닉네임")
    private String nickname;

    @Column(name = "birth", nullable = false)
    @Comment("생년월일")
    private LocalDate birth;

    @Column(name = "gender", length = 10, nullable = false)
    @Comment("성별")
    private String gender;

    @Column(name = "job", length = 255, nullable = true)
    @Comment("직업")
    private String job;

    @Column(name = "frequency", length = 50, nullable = false)
    @ColumnDefault("'NORMAL'")
    @Comment("사용 빈도")
    private String frequency;

    @Column(name = "coin_balance", nullable = false)
    @ColumnDefault("0")
    @Comment("코인 잔액")
    private Integer coinBalance;

    @Column(name = "last_login_at", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("마지막 로그인")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("가입일")
    private Instant createdAt;

    @Column(name = "fcm_token", columnDefinition = "TEXT", nullable = false)
    @Comment("FCM 토큰")
    private String fcmToken;

    private User(String personalId, String password, String nickname, LocalDate birth, String gender,
        String job, String frequency, Integer coinBalance, Instant lastLoginAt, Instant createdAt, String fcmToken) {
        this.personalId = personalId;
        this.password = password;
        this.nickname = nickname;
        this.birth = birth;
        this.gender = gender;
        this.job = job;
        this.frequency = frequency;
        this.coinBalance = coinBalance;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.fcmToken = fcmToken;
    }

    public static User of(String personalId, String password, String nickname, LocalDate birth, String gender,
        String job, String frequency, Integer coinBalance, Instant lastLoginAt, Instant createdAt, String fcmToken) {
        return new User(personalId, password, nickname, birth, gender, job, frequency, coinBalance, lastLoginAt, createdAt, fcmToken);
    }
}