package com.ssafy.Dito.domain.user.entity;

import com.ssafy.Dito.domain._common.IdentifiableEntity;
import com.ssafy.Dito.domain.user.dto.request.FrequencyReq;
import com.ssafy.Dito.domain.user.dto.request.NicknameReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "\"user\"")
public class User extends IdentifiableEntity {

    @Column(name="personal_id", length = 255, nullable = false, unique = true)
    @Comment("개인 아이디")
    private String personalId;

    @Column(name="password", length = 255, nullable = false)
    @Comment("비밀번호")
    private String password;

    @Column(name = "nickname", length = 50, nullable = false)
    @Comment("닉네임")
    private String nickname;

    @Column(name = "birth", nullable = false)
    @Comment("생년월일")
    private LocalDate birth;

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
    @ColumnDefault("0")
    @Comment("코인 잔액")
    private int coinBalance;

    @Column(name = "frequency", nullable = false)
    @ColumnDefault("'NORMAL'")
    @Comment("빈도")
    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    @Column(name = "last_login_at", nullable = true)
    @Comment("마지막 로그인")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false)
    @Comment("가입일")
    private Instant createdAt;

    @Column(name = "FCM_token", length = 255, nullable = true)
    @Comment("FCM 토큰")
    private String fcmToken;

    private User(String personalId, String password, String nickname, LocalDate birth, Gender gender,
            Job job, int coinBalance, Frequency frequency, Instant lastLoginAt, Instant createdAt, String fcmToken) {
        this.personalId = personalId;
        this.password = password;
        this.nickname = nickname;
        this.birth = birth;
        this.gender = gender;
        this.job = job == null ? Job.ETC : job;
        this.coinBalance = Math.max(0, coinBalance);
        this.frequency = frequency == null ? Frequency.NORMAL : frequency;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.fcmToken = fcmToken;
    }

    public static User of(String personalId, String password, String nickname, LocalDate birth, Gender gender,
            Job job, int coinBalance, Frequency frequency, Instant lastLoginAt, Instant createdAt, String fcmToken) {
        return new User(personalId, password, nickname, birth, gender, job, coinBalance, frequency, lastLoginAt, createdAt, fcmToken);
    }

    public void deductCoins(int amount) {
        if (amount < 0) throw new IllegalArgumentException("음수 금액은 지원하지 않습니다");
        if (this.coinBalance < amount) {
            throw new IllegalArgumentException("코인이 부족합니다");
        }
        this.coinBalance -= amount;
    }

    // 엔티티 레벨에서 닉네임 변경 로직 추가
    public void updateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 비어있을 수 없습니다");
        }
        this.nickname = nickname;
    }

    // FCM 토큰 갱신
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    // 코인 충전
    public void increaseCoins(int amount) {
        if (amount < 0) throw new IllegalArgumentException("음수 금액은 지원하지 않습니다");
        this.coinBalance += amount;
    }

    // 마지막 로그인 시간 갱신
    public void updateLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt == null ? Instant.now() : lastLoginAt;
    }

    public void updateNickname(NicknameReq req) {
        this.nickname = req.nickname();
    }

    public void updateFrequency(FrequencyReq req) {
        this.frequency = req.frequency();
    }

    public void updateCoin(int coin){
        this.coinBalance -= coin;
    }
}