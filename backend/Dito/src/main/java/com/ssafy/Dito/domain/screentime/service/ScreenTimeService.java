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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 스크린타임 관리 서비스
 * - 하이브리드 방식: Summary (빠른 조회) + Snapshot (검증/분석)
 * - 앱에서 5분마다 스크린타임 갱신
 * - 그룹별 랭킹 조회 최적화
 * - 실시간 현재 사용 중인 앱 정보 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenTimeService {

    private final ScreenTimeDailySummaryRepository summaryRepository;
    private final ScreenTimeSnapshotRepository snapshotRepository;
    private final CurrentAppUsageRepository currentAppUsageRepository;
    private final GroupChallengeRepository groupChallengeRepository;
    private final GroupParticipantRepository groupParticipantRepository;
    private final UserItemQueryRepository  userItemQueryRepository;
    private final CostumeUrlUtil costumeUrlUtil;
    private static final int MAX_PARTICIPANTS = 6;
    private final MediaSessionLogRepository mediaSessionLogRepository;

    // 메모리 캐시 사용 안 함
    // private final Map<Long, CurrentAppInfo> currentAppCache = new ConcurrentHashMap<>();

    /**
     * 스크린타임 갱신 (5분마다 호출)
     * - Summary: upsert (기존 데이터 갱신 또는 신규 생성)
     * - Snapshot: insert (항상 새로운 기록 생성)
     */
    @Transactional
    public ScreenTimeUpdateRes updateScreenTime(ScreenTimeUpdateReq request, Long userId) {
        log.info("💾 스크린타임 저장 요청 - groupId: {}, userId: {}, date: {}, totalMinutes: {}, youtubeMinutes: {}",
            request.groupId(), userId, request.date(), request.totalMinutes(),
            request.youtubeMinutes());

        // 그룹 존재 여부 확인
        GroupChallenge group = groupChallengeRepository.findById(request.groupId())
            .orElseThrow(GroupNotFoundException::new);

        // 1. Summary 갱신 (upsert)
        ScreenTimeDailySummary summary = summaryRepository
            .findByGroupIdAndUserIdAndDate(request.groupId(), userId, request.date().toString())
            .orElse(null);

        log.info("  기존 Summary 조회 결과: {}", summary != null ? "존재" : "없음");

        String status;
        if (summary == null) {
            // 신규 생성
            summary = ScreenTimeDailySummary.create(
                request.groupId(),
                userId,
                request.date(),
                request.totalMinutes(),
                request.youtubeMinutes()
            );
            status = "created";
        } else {
            // 기존 데이터 갱신
            summary.updateScreenTime(request.totalMinutes(), request.youtubeMinutes());
            status = "updated";
        }
        ScreenTimeDailySummary saved = summaryRepository.save(summary);

        log.info("  ✅ Summary 저장 완료 - id: {}, groupId: {}, userId: {}, date: {}, totalMinutes: {}, youtubeMinutes: {}",
            saved.getId(), saved.getGroupId(), saved.getUserId(), saved.getDate(), saved.getTotalMinutes(), saved.getYoutubeMinutes());

        // 2. Snapshot 생성 (항상 INSERT)
        ScreenTimeSnapshot snapshot = ScreenTimeSnapshot.create(
            request.groupId(),
            userId,
            request.date(),
            request.totalMinutes(),
            request.youtubeMinutes()
        );
        snapshotRepository.save(snapshot);

        log.info("스크린타임 갱신 완료 - userId: {}, groupId: {}, date: {}, totalMinutes: {}, youtubeMinutes: {}, status: {}",
            userId, request.groupId(), request.date(), request.totalMinutes(), request.youtubeMinutes(), status);

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
     * 현재 사용 중인 앱 정보 갱신
     * MongoDB current_app_usage 컬렉션에 저장 (upsert)
     * 안드로이드 앱에서 포그라운드 앱이 변경될 때마다 호출
     */
    @Transactional
    public void updateCurrentApp(Long userId, UpdateCurrentAppReq request) {
        log.info("📱 현재 앱 정보 갱신 - userId: {}, groupId: {}, appPackage: {}, appName: {}, duration: {}",
            userId, request.groupId(), request.appPackage(), request.appName(), request.usageDuration());

        // MongoDB에 저장 (upsert)
        CurrentAppUsage existing = currentAppUsageRepository
            .findByGroupIdAndUserId(request.groupId(), userId)
            .orElse(null);

        if (existing == null) {
            // 신규 생성
            CurrentAppUsage newAppUsage = CurrentAppUsage.create(
                request.groupId(),
                userId,
                request.appPackage(),
                request.appName(),
                request.usageDuration()
            );
            currentAppUsageRepository.save(newAppUsage);
            log.info("  ✅ 현재 앱 정보 생성 완료 - userId: {}, appName: {}", userId, request.appName());
        } else {
            // 기존 데이터 업데이트
            existing.update(request.appPackage(), request.appName(), request.usageDuration());
            currentAppUsageRepository.save(existing);
            log.info("  ✅ 현재 앱 정보 업데이트 완료 - userId: {}, appName: {}", userId, request.appName());
        }
    }



    /**
     * 그룹 챌린지 랭킹 조회
     * - YouTube 사용시간이 적은 순으로 정렬
     * - 그룹 정보 + 참여자 상세 정보 포함
     * - 현재 사용 중인 앱 정보 포함 (실시간 업데이트)
     */
    @Transactional(readOnly = true)
    public GroupRankingRes getGroupRanking(Long groupId, Long currentUserId) {
        log.info("📊 랭킹 조회 시작 - groupId: {}, currentUserId: {}", groupId, currentUserId);

        // 그룹 정보 조회
        GroupChallenge group = groupChallengeRepository.findById(groupId)
            .orElseThrow(GroupNotFoundException::new);

        LocalDate startDate = group.getStartDate();
        LocalDate endDate = group.getEndDate();
        LocalDate today = LocalDate.now();

        // 그룹 정보 구성
        Integer daysElapsed = 0;
        Integer daysTotal = 0;
        Double progressPercentage = 0.0;

        if (startDate != null && endDate != null) {
            daysTotal = (int) ChronoUnit.DAYS.between(startDate, endDate.plusDays(1));

            if (today.isBefore(startDate)) {
                daysElapsed = 0;
            } else if (today.isAfter(endDate)) {
                daysElapsed = daysTotal;
            } else {
                daysElapsed = (int) ChronoUnit.DAYS.between(startDate, today) + 1;
            }

            progressPercentage = daysTotal > 0 ? (daysElapsed * 100.0 / daysTotal) : 0.0;
        }

        // 참여자 목록 조회
        List<GroupParticipant> participants = groupParticipantRepository.findAllByIdGroup(group);
        long participantCount = participants.size();

        // Summary 데이터 조회 (챌린지 기간 내)
        List<ScreenTimeDailySummary> summaries = summaryRepository
            .findByGroupIdAndDateBetween(groupId, startDate.toString(), endDate.toString());

        log.info("📊 Summary 조회 결과 - groupId: {}, startDate: {}, endDate: {}, summaries.size: {}",
            groupId, startDate, endDate, summaries.size());

        for (ScreenTimeDailySummary s : summaries) {
            log.info("  - userId: {}, date: {}, totalMinutes: {}, youtubeMinutes: {}",
                s.getUserId(), s.getDate(), s.getTotalMinutes(), s.getYoutubeMinutes());
        }

        // MongoDB에서 현재 앱 정보 조회 (실시간 업데이트)
        List<CurrentAppUsage> currentApps = currentAppUsageRepository.findAllByGroupId(groupId);
        Map<Long, CurrentAppUsage> currentAppMap = currentApps.stream()
            .collect(Collectors.toMap(CurrentAppUsage::getUserId, app -> app));

        log.info("📱 현재 앱 정보 조회 결과 - count: {}", currentApps.size());
        for (CurrentAppUsage app : currentApps) {
            log.info("  - userId: {}, appName: {}, appPackage: {}, lastUpdated: {}",
                app.getUserId(), app.getAppName(), app.getAppPackage(), app.getLastUpdatedAt());
        }

        // 사용자별 총 스크린타임 집계
        Map<Long, Integer> userTotalTime = new HashMap<>();
        Map<Long, Integer> userYoutubeTime = new HashMap<>();

        for (ScreenTimeDailySummary summary : summaries) {
            Long userId = summary.getUserId();

            // totalMinutes 집계
            Integer totalMinutes = summary.getTotalMinutes();
            if (totalMinutes == null) {
                totalMinutes = 0;
            }
            userTotalTime.merge(userId, totalMinutes, Integer::sum);

//            // youtubeMinutes 집계
//            Integer youtubeMinutes = summary.getYoutubeMinutes();
//            if (youtubeMinutes == null) {
//                youtubeMinutes = 0;
//            }
//            userYoutubeTime.merge(userId, youtubeMinutes, Integer::sum);
        }

        log.info("📊 집계된 사용자별 총 스크린타임: {}", userTotalTime);
        log.info("📊 집계된 사용자별 YouTube 시간: {}", userYoutubeTime);

        // 참여자별 베팅 코인 정보
        Map<Long, Integer> userBetCoins = participants.stream()
            .collect(Collectors.toMap(
                p -> p.getId().getUser().getId(),
                GroupParticipant::getBetCoins
            ));

        // 랭킹 계산 (YouTube 사용시간 적은 순)
        final int finalDaysElapsed = daysElapsed;
        AtomicInteger rankCounter = new AtomicInteger(1);

        List<GroupRankingRes.ParticipantRank> rankings = participants.stream()
//                .map(participant -> {
//                    Long uid = participant.getId().getUser().getId();
//                    String nickname = participant.getId().getUser().getNickname();
//
//                    // 🔥 1) snapshot 불러오기
//                    List<ScreenTimeSnapshot> snaps =
//                            snapshotRepository.findByGroupIdAndUserIdAndDateBetweenOrderByRecordedAtAsc(
//                                    groupId,
//                                    uid,
//                                    startDate.toString(),
//                                    endDate.toString()
//                            );
//
//                    // 🔥 2) snapshot 기반 유튜브 시간 계산
//                    int youtubeMinutesAccurate = calculateYoutubeFromSnapshots(snaps);
//
//                    // 기존 요약(totalMinutes)은 summary로 적절함
//                    Integer totalMinutes = userTotalTime.getOrDefault(uid, 0);
//                    Integer betCoins = userBetCoins.getOrDefault(uid, 0);
//
//                    return Map.entry(uid,
//                            new RankingData(
//                                    nickname,
//                                    totalMinutes,
//                                    youtubeMinutesAccurate,
//                                    betCoins
//                            )
//                    );
//                })
//
//                .sorted(Map.Entry.comparingByValue()) // RankingData의 Comparable 사용 (YouTube 시간 기준)
                .map(participant -> {
                    Long uid = participant.getId().getUser().getId();
                    String nickname = participant.getId().getUser().getNickname();

                    // ================================
                    // 1) 기존 snapshot 기반 제거됨
                    // ================================

                    // ================================
                    // 2) MediaSession 이벤트 읽기 추가
                    //    교육용 영상은 제외됨
                    // ================================
                    List<MediaSessionEventDocument> events =
                            mediaSessionLogRepository.findByUserIdAndEventDateBetween(
                                    uid, startDate, endDate
                            ); // ✅ 추가됨

                    long youtubeSeconds = 0;

                    for (MediaSessionEventDocument e : events) {

                        // 교육용 영상은 시간 제외
                        if (Boolean.TRUE.equals(e.getIsEducational())) {
                            continue; // ✅ 변경됨
                        }

                        // 정상 watchTime 누적
                        youtubeSeconds += (e.getWatchTime() != null ? e.getWatchTime() : 0);
                    }

                    int youtubeMinutesAccurate = (int) (youtubeSeconds / 60); // 최종 분 단위
                    // ================================

                    Integer totalMinutes = userTotalTime.getOrDefault(uid, 0);
                    Integer betCoins = userBetCoins.getOrDefault(uid, 0);

                    return Map.entry(uid,
                            new RankingData(
                                    nickname,
                                    totalMinutes,
                                    youtubeMinutesAccurate, // snapshot → mediaSessionEvents 기반으로 변경됨
                                    betCoins
                            )
                    );
                })
                .sorted(Map.Entry.comparingByValue())
            .map(entry -> {
                Long uid = entry.getKey();
                RankingData data = entry.getValue();

                int rank = rankCounter.getAndIncrement();
                double avgTotalMinutes = finalDaysElapsed > 0 ? data.totalMinutes / (double) finalDaysElapsed : 0.0;
                double avgYoutubeMinutes = finalDaysElapsed > 0 ? data.youtubeMinutes / (double) finalDaysElapsed : 0.0;

                // 1등은 총 베팅 코인을 모두 가져감
                Integer potentialPrize = (rank == 1) ? group.getTotalBetCoins() : 0;

                // MongoDB에서 현재 앱 정보 조회 (실시간)
                CurrentAppUsage currentApp = currentAppMap.get(uid);
                String currentAppPackage = currentApp != null ? currentApp.getAppPackage() : null;
                String currentAppName = currentApp != null ? currentApp.getAppName() : null;
                // 장착된 코스튬 아이템 ID 조회 + 이미지 url 조회
                Integer costumeItemId = null;
                String costumeImageUrl = null;
                UserItem equippedCostume = userItemQueryRepository.getEquippedItem(uid, Type.COSTUME);
                if (equippedCostume != null) {
                    Long itemId = equippedCostume.getId().getItem().getId();
                    costumeItemId = itemId != null ? itemId.intValue() : null;
                    String baseImageUrl = equippedCostume.getId().getItem().getImgUrl();
                    costumeImageUrl = costumeUrlUtil.getCostumeUrl(baseImageUrl, uid, false);
                }
                log.info("  - 랭킹 {}위: userId={}, nickname={}, costumeUrl={}, youtubeMinutes={}, avgYoutubeMinutes={}m, currentApp={}",
                    rank, uid, data.nickname, costumeImageUrl,data.youtubeMinutes, (int)avgYoutubeMinutes, currentAppName);

                return GroupRankingRes.ParticipantRank.of(
                    rank,
                    uid,
                    data.nickname,
                    costumeImageUrl,
                    costumeItemId,
                    formatTime(data.youtubeMinutes),
                    formatTime((int) avgYoutubeMinutes),
                    data.betCoins,
                    potentialPrize,
                    uid.equals(currentUserId),
                    currentAppPackage,
                    currentAppName
                );
            })
            .collect(Collectors.toList());

        log.info("📊 최종 랭킹 (YouTube 시간 기준): {}", rankings);

        return GroupRankingRes.of(rankings);
    }

    /**
     * 시간 포맷팅 (분 -> "Xh Ym")
     */
    private String formatTime(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        if (hours > 0 && minutes > 0) {
            return hours + "h " + minutes + "m";
        } else if (hours > 0) {
            return hours + "h";
        } else {
            return minutes + "m";
        }
    }

    /**
     * 랭킹 정렬을 위한 내부 데이터 클래스
     * YouTube 사용시간 기준으로 정렬
     */
    private record RankingData(String nickname, int totalMinutes, int youtubeMinutes, int betCoins)
        implements Comparable<RankingData> {

        @Override
        public int compareTo(RankingData other) {
            // YouTube 사용시간 적은 순으로 정렬
            int youtubeCompare = Integer.compare(this.youtubeMinutes, other.youtubeMinutes);

            // YouTube 시간이 같으면 총 스크린타임으로 비교
            if (youtubeCompare == 0) {
                return Integer.compare(this.totalMinutes, other.totalMinutes);
            }

            return youtubeCompare;
        }
    }


    /**
     * 특정 사용자의 특정 기간 스크린타임 조회
     */
    @Transactional(readOnly = true)
    public List<ScreenTimeDailySummary> getUserScreenTime(Long groupId, Long userId,
        LocalDate startDate, LocalDate endDate) {
        return summaryRepository.findByGroupIdAndUserIdAndDateBetween(
            groupId, userId, startDate.toString(), endDate.toString()
        );
    }

    /**
     * 특정 사용자의 스크린타임 Snapshot 조회 (검증용)
     */
    @Transactional(readOnly = true)
    public List<ScreenTimeSnapshot> getUserSnapshots(Long userId, LocalDate date) {
        return snapshotRepository.findByUserIdAndDateOrderByRecordedAtDesc(userId, date);
    }

    private int calculateYoutubeFromSnapshots(List<ScreenTimeSnapshot> snaps) {
        int ytSeconds = 0;
        int prev = -1;

        for (ScreenTimeSnapshot s : snaps) {
            int cur = s.getYoutubeMinutes();

            if (prev == -1) {
                ytSeconds += cur * 60; // 첫 스냅샷 포함
            } else {
                int d = cur - prev;
                if (d > 0) ytSeconds += d * 60;
            }
            prev = cur;
        }

        return ytSeconds / 60;
    }

}