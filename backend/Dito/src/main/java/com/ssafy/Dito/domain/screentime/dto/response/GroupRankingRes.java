package com.ssafy.Dito.domain.screentime.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * 그룹 챌린지 랭킹 응답 DTO
 */
@Schema(description = "그룹 챌린지 랭킹 응답")
public record GroupRankingRes(

    @Schema(description = "그룹 정보")
    GroupInfo groupInfo,

    @Schema(description = "참여자 랭킹 목록")
    List<ParticipantRank> rankings
) {

    @Schema(description = "그룹 정보")
    public record GroupInfo(

        @Schema(description = "그룹 ID", example = "1")
        Long groupId,

        @Schema(description = "그룹 이름", example = "한 주 디톡스 챌린지")
        String groupName,

        @Schema(description = "시작 날짜", example = "2025-11-02")
        LocalDate startDate,

        @Schema(description = "종료 날짜", example = "2025-11-16")
        LocalDate endDate,

        @Schema(description = "목표 설명", example = "하루 평균 스크린 타임 4시간 이하")
        String goalDescription,

        @Schema(description = "페널티 설명", example = "꼴찌는 다 같이 치킨 쏘기")
        String penaltyDescription,

        @Schema(description = "총 베팅 코인", example = "250")
        Integer totalBetCoins,

        @Schema(description = "챌린지 상태", example = "in_progress", allowableValues = {"pending", "in_progress", "completed", "cancelled"})
        String status,

        @Schema(description = "경과 일수", example = "3")
        Integer daysElapsed,

        @Schema(description = "총 일수", example = "14")
        Integer daysTotal,

        @Schema(description = "진행률 (%)", example = "21.4")
        Double progressPercentage,

        @Schema(description = "참여자 수", example = "2")
        Integer participantCount,

        @Schema(description = "최대 참여자 수", example = "6")
        Integer maxParticipants
    ) {
        public static GroupInfo of(Long groupId, String groupName, LocalDate startDate, LocalDate endDate,
                                   String goalDescription, String penaltyDescription, Integer totalBetCoins,
                                   String status, Integer daysElapsed, Integer daysTotal, Double progressPercentage,
                                   Integer participantCount, Integer maxParticipants) {
            return new GroupInfo(groupId, groupName, startDate, endDate, goalDescription, penaltyDescription,
                totalBetCoins, status, daysElapsed, daysTotal, progressPercentage, participantCount, maxParticipants);
        }
    }

    @Schema(description = "참여자 랭킹 정보")
    public record ParticipantRank(

        @Schema(description = "순위", example = "1")
        Integer rank,

        @Schema(description = "사용자 ID", example = "123")
        Long userId,

        @Schema(description = "닉네임", example = "이○○")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://...")
        String profileImage,

        @Schema(description = "총 스크린타임 (포맷)", example = "10h 30m")
        String totalScreenTimeFormatted,

        @Schema(description = "일평균 스크린타임 (포맷)", example = "3h 30m")
        String avgDailyScreenTimeFormatted,

        @Schema(description = "베팅 코인", example = "150")
        Integer betCoins,

        @Schema(description = "잠재 상금", example = "250")
        Integer potentialPrize,

        @Schema(description = "본인 여부", example = "true")
        Boolean isMe,

        @Schema(description = "현재 사용 중인 앱 패키지명", example = "com.google.android.youtube")
        String currentAppPackage,

        @Schema(description = "현재 사용 중인 앱 이름", example = "YouTube")
        String currentAppName
    ) {
        public static ParticipantRank of(Integer rank, Long userId, String nickname, String profileImage,
                                         String totalScreenTimeFormatted, String avgDailyScreenTimeFormatted,
                                         Integer betCoins, Integer potentialPrize, Boolean isMe,
                                         String currentAppPackage, String currentAppName) {
            return new ParticipantRank(rank, userId, nickname, profileImage, totalScreenTimeFormatted,
                avgDailyScreenTimeFormatted, betCoins, potentialPrize, isMe, currentAppPackage, currentAppName);
        }
    }

    public static GroupRankingRes of(GroupInfo groupInfo, List<ParticipantRank> rankings) {
        return new GroupRankingRes(groupInfo, rankings);
    }
}
