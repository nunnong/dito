package com.ssafy.Dito.domain.screentime.service;

import com.ssafy.Dito.domain._common.CostumeUrlUtil;
import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import com.ssafy.Dito.domain.groups.entity.GroupParticipant;
import com.ssafy.Dito.domain.groups.exception.GroupNotFoundException;
import com.ssafy.Dito.domain.groups.repository.GroupChallengeRepository;
import com.ssafy.Dito.domain.groups.repository.GroupParticipantRepository;
import com.ssafy.Dito.domain.item.entity.Type;
import com.ssafy.Dito.domain.log.mediaSessionEvent.document.MediaSessionEventDocument;
import com.ssafy.Dito.domain.log.mediaSessionEvent.repository.MediaSessionLogRepository;
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
    private final MediaSessionLogRepository mediaSessionLogRepository;   // ⭐ 추가됨

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
        if (summary == null) {
            summary = ScreenTimeDailySummary.create(
                    request.groupId(),
                    userId,
                    request.date(),
                    request.totalMinutes(),
                    request.youtubeMinutes()
            );
            status = "created";
        } else {
            summary.updateScreenTime(request.totalMinutes(), request.youtubeMinutes());
            status = "updated";
        }
        summaryRepository.save(summary);

        // Snapshot INSERT (기본 유지)
        snapshotRepository.save(
                ScreenTimeSnapshot.create(
                        request.groupId(),
                        userId,
                        request.date(),
                        request.totalMinutes(),
                        request.youtubeMinutes()
                )
        );

        return ScreenTimeUpdateRes.of(
                request.groupId(),
                userId,
                request.date(),
                request.totalMinutes(),
                request.youtubeMinutes(),
                status
        );
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

        if (existing == null) {
            currentAppUsageRepository.save(CurrentAppUsage.create(
                    request.groupId(),
                    userId,
                    request.appPackage(),
                    request.appName(),
                    request.usageDuration()

            ));
        } else {
            existing.update(request.appPackage(), request.appName(), request.usageDuration());
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

        AtomicInteger rankCounter = new AtomicInteger(1);

        final int finalDaysElapsed = daysElapsed;

        List<GroupRankingRes.ParticipantRank> rankings = participants.stream()
                .map(participant -> {

                    Long uid = participant.getId().getUser().getId();
                    String nickname = participant.getId().getUser().getNickname();

                    // ============================
                    // ⭐ 전체 스크린타임 / 유튜브 시간 계산
                    // ============================
//                    List<MediaSessionEventDocument> events =
//                            mediaSessionLogRepository.findByUserIdAndEventDateBetween(
//                                    uid, startDate, endDate
//                            );

                    final LocalDate queryStart = startDate;
                    final LocalDate queryEnd = (endDate != null ? endDate.plusDays(1) : null);

                    List<MediaSessionEventDocument> events =
                            (queryStart != null && queryEnd != null)
                                    ? mediaSessionLogRepository.findByUserIdAndEventDateBetween(
                                    uid, queryStart, queryEnd
                            )
                                    : Collections.emptyList();


                    long totalSeconds = 0;
                    long youtubeSeconds = 0;

                    MediaSessionEventDocument latestYoutubeEvent = null;

                    for (MediaSessionEventDocument e : events) {

                        long watch = (e.getWatchTime() != null ? e.getWatchTime() : 0);

                        // 전체 스크린타임 누적
                        totalSeconds += watch;

                        // YouTube 감지
                        boolean isYoutube =
                                e.getPackageName() != null &&
                                        e.getPackageName().contains("youtube");

                        if (isYoutube) {

                            // ⭐ 가장 최근 YouTube 이벤트 추적
                            if (latestYoutubeEvent == null ||
                                    e.getEventTimestamp() > latestYoutubeEvent.getEventTimestamp()) {
                                latestYoutubeEvent = e;
                            }

                            // 교육용 제외하고 유튜브 시간 계산
                            if (!Boolean.TRUE.equals(e.getIsEducational())) {
                                youtubeSeconds += watch;
                            }
                        }
                    }

                    int totalMinutesAccurate = (int) (totalSeconds / 60);
                    int youtubeMinutesAccurate = (int) (youtubeSeconds / 60);

                    boolean latestIsEducational =
                            latestYoutubeEvent != null &&
                                    Boolean.TRUE.equals(latestYoutubeEvent.getIsEducational());

                    Integer betCoins = participant.getBetCoins();

                    return Map.entry(
                            uid,
                            new RankingData(
                                    nickname,
                                    totalMinutesAccurate,     // ⭐ 변경
                                    youtubeMinutesAccurate,   // ⭐ 변경
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

                    double avgTotalMinutes =
                            finalDaysElapsed > 0 ? data.totalMinutes() / (double) finalDaysElapsed : 0.0;

                    double avgYoutubeMinutes =
                            finalDaysElapsed > 0 ? data.youtubeMinutes() / (double) finalDaysElapsed : 0.0;


                    Integer potentialPrize = (rank == 1) ? group.getTotalBetCoins() : 0;

                    CurrentAppUsage currentApp = currentAppMap.get(uid);

                    // 코스튬
                    UserItem equippedCostume = userItemQueryRepository.getEquippedItem(uid, Type.COSTUME);

                    Integer itemId = null;
                    String costumeUrl = null;

                    if (equippedCostume != null) {
                        Long costumeItemId = equippedCostume.getId().getItem().getId();  // ⭐ Long 으로 받음
                        itemId = costumeItemId != null ? costumeItemId.intValue() : null; // ⭐ 안전 변환
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
                            formatTime(data.totalMinutes()),
                            formatTime((int) avgTotalMinutes),
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
            int totalMinutes,
            int youtubeMinutes,
            int betCoins,
            boolean latestIsEducational
    ) implements Comparable<RankingData> {
        @Override
        public int compareTo(RankingData other) {
            int compareYoutube = Integer.compare(this.youtubeMinutes, other.youtubeMinutes);
            if (compareYoutube == 0)
                return Integer.compare(this.totalMinutes, other.totalMinutes);
            return compareYoutube;
        }
    }

}
