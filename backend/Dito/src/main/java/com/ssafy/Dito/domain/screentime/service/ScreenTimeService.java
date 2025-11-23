package com.ssafy.Dito.domain.screentime.service;

import com.ssafy.Dito.domain._common.CostumeUrlUtil;
import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import com.ssafy.Dito.domain.groups.entity.GroupParticipant;
import com.ssafy.Dito.domain.groups.exception.GroupNotFoundException;
import com.ssafy.Dito.domain.groups.repository.GroupChallengeRepository;
import com.ssafy.Dito.domain.groups.repository.GroupParticipantRepository;
import com.ssafy.Dito.domain.item.entity.Type;
import com.ssafy.Dito.domain.screentime.document.CurrentAppUsage;
import com.ssafy.Dito.domain.screentime.document.ScreenTimeDailySummary;
import com.ssafy.Dito.domain.screentime.document.ScreenTimeSnapshot;
import com.ssafy.Dito.domain.screentime.dto.request.ScreenTimeUpdateReq;
import com.ssafy.Dito.domain.screentime.dto.request.UpdateCurrentAppReq;
import com.ssafy.Dito.domain.screentime.dto.response.GroupRankingRes;
import com.ssafy.Dito.domain.screentime.dto.response.ScreenTimeUpdateRes;
import com.ssafy.Dito.domain.screentime.repository.CurrentAppUsageRepository;
import com.ssafy.Dito.domain.screentime.repository.ScreenTimeDailySummaryRepository;
import com.ssafy.Dito.domain.screentime.repository.ScreenTimeSnapshotRepository;
import com.ssafy.Dito.domain.user.userItem.entity.UserItem;
import com.ssafy.Dito.domain.user.userItem.repository.UserItemQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenTimeService {

    private final ScreenTimeDailySummaryRepository summaryRepository;
    private final ScreenTimeSnapshotRepository snapshotRepository;
    private final CurrentAppUsageRepository currentAppUsageRepository;
    private final GroupChallengeRepository groupChallengeRepository;
    private final GroupParticipantRepository groupParticipantRepository;
    private final UserItemQueryRepository userItemQueryRepository;
    private final CostumeUrlUtil costumeUrlUtil;

    private static final int MAX_PARTICIPANTS = 6;

    /**
     * 스크린타임 갱신 (5분마다 호출)
     */
    @Transactional
    public ScreenTimeUpdateRes updateScreenTime(ScreenTimeUpdateReq request, Long userId) {

        log.info("💾 스크린타임 저장 요청 - groupId: {}, userId: {}, date: {}, totalMinutes: {}, youtubeMinutes: {}",
                request.groupId(), userId, request.date(), request.totalMinutes(), request.youtubeMinutes());

        GroupChallenge group = groupChallengeRepository.findById(request.groupId())
                .orElseThrow(GroupNotFoundException::new);

        // Summary upsert (기본 구조 유지)
        ScreenTimeDailySummary summary = summaryRepository
                .findByGroupIdAndUserIdAndDate(request.groupId(), userId, request.date().toString())
                .orElse(null);

        String status;
        int reportedTotalMinutes = request.totalMinutes() != null ? request.totalMinutes() : 0;
        int reportedYoutubeMinutes = resolveReportedYoutubeMinutes(summary, request);

        if (summary == null) {
            summary = ScreenTimeDailySummary.create(
                    request.groupId(),
                    userId,
                    request.date(),
                    reportedTotalMinutes,
                    reportedYoutubeMinutes
            );
            status = "created";
        } else {
            int previousTotalReport = summary.getLastReportedTotalMinutes() != null
                    ? summary.getLastReportedTotalMinutes()
                    : reportedTotalMinutes;
            int previousYoutubeReport = summary.getLastReportedYoutubeMinutes() != null
                    ? summary.getLastReportedYoutubeMinutes()
                    : reportedYoutubeMinutes;

            int deltaTotalMinutes = Math.max(0, reportedTotalMinutes - previousTotalReport);
            int deltaYoutubeMinutes = Math.max(0, reportedYoutubeMinutes - previousYoutubeReport);

            summary.updateScreenTime(deltaTotalMinutes, deltaYoutubeMinutes, reportedTotalMinutes, reportedYoutubeMinutes);
            status = "updated";
        }
        summaryRepository.save(summary);

        // Snapshot INSERT (기본 유지)
        snapshotRepository.save(
                ScreenTimeSnapshot.create(
                        request.groupId(),
                        userId,
                        request.date(),
                        summary.getTotalMinutes(),
                        summary.getYoutubeMinutes()
                )
        );

        return ScreenTimeUpdateRes.of(
                request.groupId(),
                userId,
                request.date(),
                summary.getTotalMinutes(),
                summary.getYoutubeMinutes(),
                status
        );
    }

    private int resolveReportedYoutubeMinutes(ScreenTimeDailySummary summary, ScreenTimeUpdateReq request) {
        if (request.youtubeMinutes() != null) {
            return request.youtubeMinutes();
        }
        if (summary != null) {
            if (summary.getLastReportedYoutubeMinutes() != null) {
                return summary.getLastReportedYoutubeMinutes();
            }
            if (summary.getInitialYoutubeMinutes() != null) {
                return summary.getInitialYoutubeMinutes();
            }
        }
        return 0;
    }

    /**
     * 현재 사용 중인 앱 갱신
     */
    @Transactional
    public void updateCurrentApp(Long userId, UpdateCurrentAppReq request) {
        log.info("📱 현재 앱 정보 갱신 - userId: {}, groupId: {}, appPackage: {}, appName: {}, duration: {}",
                userId, request.groupId(), request.appPackage(), request.appName(), request.usageDuration());

        CurrentAppUsage existing =
                currentAppUsageRepository.findByGroupIdAndUserId(request.groupId(), userId).orElse(null);

        String mediaEventId = request.mediaEventId();
        Long mediaEventTimestamp = request.mediaEventTimestamp();
        Boolean mediaEducational = request.mediaEducational();

        if (existing == null) {
            currentAppUsageRepository.save(CurrentAppUsage.create(
                    request.groupId(),
                    userId,
                    request.appPackage(),
                    request.appName(),
                    request.usageDuration(),
                    mediaEventId,
                    mediaEventTimestamp,
                    mediaEducational
            ));
        } else {
            existing.update(
                    request.appPackage(),
                    request.appName(),
                    request.usageDuration(),
                    mediaEventId,
                    mediaEventTimestamp,
                    mediaEducational
            );
            currentAppUsageRepository.save(existing);
        }
    }

    /**
     * 그룹 랭킹 — ⭐ MediaSessionEvent 기반 totalMinutes / youtubeMinutes 완전 계산
     */
    @Transactional(readOnly = true)
    public GroupRankingRes getGroupRanking(Long groupId, Long currentUserId) {

        GroupChallenge group = groupChallengeRepository.findById(groupId)
                .orElseThrow(GroupNotFoundException::new);

        LocalDate startDate = group.getStartDate();
        LocalDate endDate = group.getEndDate();
        LocalDate today = LocalDate.now();

        int daysElapsed = 0;
        int daysTotal = 0;

        if (startDate != null && endDate != null) {
            daysTotal = (int) ChronoUnit.DAYS.between(startDate, endDate.plusDays(1));

            if (today.isBefore(startDate)) {
                daysElapsed = 0;
            } else if (today.isAfter(endDate)) {
                daysElapsed = daysTotal;
            } else {
                daysElapsed = (int) ChronoUnit.DAYS.between(startDate, today) + 1;
            }
        }

        List<GroupParticipant> participants =
                groupParticipantRepository.findAllByIdGroup(group);

        List<CurrentAppUsage> currentApps =
                currentAppUsageRepository.findAllByGroupId(groupId);

        Map<Long, CurrentAppUsage> currentAppMap =
                currentApps.stream().collect(Collectors.toMap(CurrentAppUsage::getUserId, a -> a));

        // ⭐ ScreenTimeDailySummary에서 기간별 데이터 조회
        String startDateStr = startDate != null ? startDate.toString() : null;
        String endDateStr = endDate != null ? endDate.toString() : null;

        List<ScreenTimeDailySummary> allSummaries =
                (startDateStr != null && endDateStr != null)
                        ? summaryRepository.findByGroupIdAndDateBetween(groupId, startDateStr, endDateStr)
                        : Collections.emptyList();

        // userId별로 그룹화
        Map<Long, List<ScreenTimeDailySummary>> summaryMap = allSummaries.stream()
                .collect(Collectors.groupingBy(ScreenTimeDailySummary::getUserId));

        AtomicInteger rankCounter = new AtomicInteger(1);
        final int finalDaysElapsed = daysElapsed;

        List<GroupRankingRes.ParticipantRank> rankings = participants.stream()
                .map(participant -> {

                    Long uid = participant.getId().getUser().getId();
                    String nickname = participant.getId().getUser().getNickname();

                    // ⭐ ScreenTimeDailySummary에서 유튜브 시간 합산
                    int youtubeMinutes = summaryMap.getOrDefault(uid, Collections.emptyList())
                            .stream()
                            .mapToInt(s -> s.getYoutubeMinutes() != null ? s.getYoutubeMinutes() : 0)
                            .sum();

                    CurrentAppUsage currentApp = currentAppMap.get(uid);
                    boolean watchingYoutube = currentApp != null
                            && currentApp.getAppPackage() != null
                            && currentApp.getAppPackage().contains("youtube");

                    Boolean currentEducationalFlag = currentApp != null ? currentApp.getLastIsEducational() : null;
                    boolean latestIsEducational = watchingYoutube && Boolean.TRUE.equals(currentEducationalFlag);

                    Integer betCoins = participant.getBetCoins();

                    return Map.entry(
                            uid,
                            new RankingData(
                                    nickname,
                                    youtubeMinutes,     // ⭐ Summary에서 가져온 유튜브 시간
                                    betCoins,
                                    latestIsEducational
                            )
                    );
                })
                .sorted(Map.Entry.comparingByValue())
                .map(entry -> {

                    Long uid = entry.getKey();
                    RankingData data = entry.getValue();

                    int rank = rankCounter.getAndIncrement();

                    double avgYoutubeMinutes =
                            finalDaysElapsed > 0 ? data.youtubeMinutes() / (double) finalDaysElapsed : 0.0;

                    Integer potentialPrize = (rank == 1) ? group.getTotalBetCoins() : 0;

                    CurrentAppUsage currentApp = currentAppMap.get(uid);

                    // 코스튬
                    UserItem equippedCostume = userItemQueryRepository.getEquippedItem(uid, Type.COSTUME);

                    Integer itemId = null;
                    String costumeUrl = null;

                    if (equippedCostume != null) {
                        Long costumeItemId = equippedCostume.getId().getItem().getId();
                        itemId = costumeItemId != null ? costumeItemId.intValue() : null;
                        costumeUrl = costumeUrlUtil.getCostumeUrl(
                                equippedCostume.getId().getItem().getImgUrl(),
                                uid,
                                false
                        );
                    }

                    return GroupRankingRes.ParticipantRank.of(
                            rank,
                            uid,
                            data.nickname(),
                            costumeUrl,
                            itemId,
                            formatTime(data.youtubeMinutes()),      // ⭐ 유튜브 시간만 표시
                            formatTime((int) avgYoutubeMinutes),    // ⭐ 평균도 유튜브만
                            data.betCoins(),
                            potentialPrize,
                            uid.equals(currentUserId),
                            currentApp != null ? currentApp.getAppPackage() : null,
                            currentApp != null ? currentApp.getAppName() : null,
                            data.latestIsEducational()
                    );
                })
                .collect(Collectors.toList());

        return GroupRankingRes.of(rankings);
    }

    private String formatTime(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        if (hours > 0 && minutes > 0) return hours + "h " + minutes + "m";
        if (hours > 0) return hours + "h";
        return minutes + "m";
    }

    /**
     * 정렬기준: YouTube → total
     */
    private record RankingData(
            String nickname,
            int youtubeMinutes,  // ⭐ totalMinutes 제거
            int betCoins,
            boolean latestIsEducational
    ) implements Comparable<RankingData> {

        @Override
        public int compareTo(RankingData other) {
            return Integer.compare(this.youtubeMinutes, other.youtubeMinutes);
        }
    }
}
