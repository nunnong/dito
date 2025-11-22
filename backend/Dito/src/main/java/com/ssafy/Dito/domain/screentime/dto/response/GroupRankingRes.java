package com.ssafy.Dito.domain.screentime.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * 그룹 챌린지 랭킹 응답 DTO
 */
@Schema(description = "그룹 챌린지 랭킹 응답")
public record GroupRankingRes(

    @Schema(description = "참여자 랭킹 목록")
    List<ParticipantRank> rankings
){

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

        @Schema(description = "장착된 코스튬 아이템 ID", example = "4")
        @JsonProperty("itemId")
        Integer costumeItemId,

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
        String currentAppName,

        boolean isEducational
    ) {
        public static ParticipantRank of(Integer rank, Long userId, String nickname, String profileImage, Integer costumeItemId,
                                         String totalScreenTimeFormatted, String avgDailyScreenTimeFormatted,
                                         Integer betCoins, Integer potentialPrize, Boolean isMe,
                                         String currentAppPackage, String currentAppName, boolean isEducational) {
            return new ParticipantRank(rank, userId, nickname, profileImage, costumeItemId,totalScreenTimeFormatted,
                avgDailyScreenTimeFormatted, betCoins, potentialPrize, isMe, currentAppPackage, currentAppName, isEducational);
        }
    }

    public static GroupRankingRes of(List<ParticipantRank> rankings) {
        return new GroupRankingRes(rankings);
    }
}
