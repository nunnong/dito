package com.ssafy.Dito.domain.screentime.service;

import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import com.ssafy.Dito.domain.groups.entity.GroupParticipant;
import com.ssafy.Dito.domain.groups.exception.GroupNotFoundException;
import com.ssafy.Dito.domain.groups.repository.GroupChallengeRepository;
import com.ssafy.Dito.domain.groups.repository.GroupParticipantRepository;
import com.ssafy.Dito.domain.screentime.document.ScreenTimeDailySummary;
import com.ssafy.Dito.domain.screentime.document.ScreenTimeSnapshot;
import com.ssafy.Dito.domain.screentime.dto.request.ScreenTimeUpdateReq;
import com.ssafy.Dito.domain.screentime.dto.response.GroupRankingRes;
import com.ssafy.Dito.domain.screentime.dto.response.ScreenTimeUpdateRes;
import com.ssafy.Dito.domain.screentime.repository.ScreenTimeDailySummaryRepository;
import com.ssafy.Dito.domain.screentime.repository.ScreenTimeSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 스크린타임 관리 서비스
 * - 하이브리드 방식: Summary (빠른 조회) + Snapshot (검증/분석)
 * - 앱에서 5분마다 스크린타임 갱신
 * - 그룹별 랭킹 조회 최적화
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenTimeService {

    private final ScreenTimeDailySummaryRepository summaryRepository;
    private final ScreenTimeSnapshotRepository snapshotRepository;
    private final GroupChallengeRepository groupChallengeRepository;
    private final GroupParticipantRepository groupParticipantRepository;

    private static final int MAX_PARTICIPANTS = 6;

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

        log.info("스크린타임 갱신 완료 - userId: {}, groupId: {}, date: {}, totalMinutes: {}, youtubeMinutes: {},status: {}",
            userId, request.groupId(), request.date(), request.totalMinutes(), request.youtubeMinutes(),status);

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
     * 그룹 챌린지 랭킹 조회
     * - 스크린타임이 적은 순으로 정렬
     * - 그룹 정보 + 참여자 상세 정보 포함
     */
    @Transactional(readOnly = true)
    public GroupRankingRes getGroupRanking(Long groupId, Long currentUserId) {
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

        GroupRankingRes.GroupInfo groupInfo = GroupRankingRes.GroupInfo.of(
            group.getId(),
            group.getGroupName(),
            startDate,
            endDate,
            group.getGoalDescription(),
            group.getPenaltyDescription(),
            group.getTotalBetCoins(),
            group.getStatus(),
            daysElapsed,
            daysTotal,
            Math.round(progressPercentage * 10.0) / 10.0, // 소수점 1자리
            (int) participantCount,
            MAX_PARTICIPANTS
        );

        // 챌린지가 시작하지 않았으면 빈 랭킹 반환
        if (startDate == null || endDate == null) {
            return GroupRankingRes.of(groupInfo, List.of());
        }

        // Summary 데이터 조회 (챌린지 기간 내)
        List<ScreenTimeDailySummary> summaries = summaryRepository
            .findByGroupIdAndDateBetween(groupId, startDate.toString(), endDate.toString());

        log.info("📊 Summary 조회 결과 - groupId: {}, startDate: {}, endDate: {}, summaries.size: {}",
            groupId, startDate, endDate, summaries.size());

        for (ScreenTimeDailySummary s : summaries) {
            log.info("  - userId: {}, date: {}, totalMinutes: {}",
                s.getUserId(), s.getDate(), s.getTotalMinutes());
        }

        // 사용자별 총 유튜브 스크린타임 집계 (변경)
        Map<Long, Integer> userYoutubeTime = new HashMap<>();
        for (ScreenTimeDailySummary summary : summaries) {

            Integer youtubeMinutes = summary.getYoutubeMinutes();
            if (youtubeMinutes != null) {
                youtubeMinutes = 0;
            }

            userYoutubeTime.merge(
                summary.getUserId(),
                youtubeMinutes,
                Integer::sum
            );
        }

        // 참여자별 베팅 코인 정보
        Map<Long, Integer> userBetCoins = participants.stream()
            .collect(Collectors.toMap(
                p -> p.getId().getUser().getId(),
                GroupParticipant::getBetCoins
            ));

        // 랭킹 계산 (스크린타임 적은 순)
        final int finalDaysElapsed = daysElapsed;
        AtomicInteger rankCounter = new AtomicInteger(1);
        List<GroupRankingRes.ParticipantRank> rankings = participants.stream()
            .map(participant -> {
                Long uid = participant.getId().getUser().getId();
                String nickname = participant.getId().getUser().getNickname();
                Integer youtubeMinutes = userYoutubeTime.getOrDefault(uid, 0);
                Integer betCoins = userBetCoins.getOrDefault(uid, 0);

                return Map.entry(uid, new RankingData(nickname, youtubeMinutes, betCoins));
            })
            .sorted(Map.Entry.comparingByValue()) // RankingData의 Comparable 사용
            .map(entry -> {
                Long uid = entry.getKey();
                RankingData data = entry.getValue();

                int rank = rankCounter.getAndIncrement();
                double avgMinutes = finalDaysElapsed > 0 ? data.totalMinutes / (double) finalDaysElapsed : 0.0;

                // 1등은 총 베팅 코인을 모두 가져감
                Integer potentialPrize = (rank == 1) ? group.getTotalBetCoins() : 0;

                return GroupRankingRes.ParticipantRank.of(
                    rank,
                    uid,
                    data.nickname,
                    null, // 프로필 이미지 (User 엔티티에 없음)
                    formatTime(data.totalMinutes),
                    formatTime((int) avgMinutes),
                    data.betCoins,
                    potentialPrize,
                    uid.equals(currentUserId)
                );
            })
            .collect(Collectors.toList());

        return GroupRankingRes.of(groupInfo, rankings);
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
     */
    private record RankingData(String nickname, int totalMinutes, int betCoins)
        implements Comparable<RankingData> {

        @Override
        public int compareTo(RankingData other) {
            // 스크린타임 적은 순으로 정렬
            return Integer.compare(this.totalMinutes, other.totalMinutes);
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
}