package com.ssafy.Dito.domain.screentime.repository;

import com.ssafy.Dito.domain.screentime.document.ScreenTimeSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for screen time snapshots
 * 스크린타임 이력 데이터 관리 (검증 및 분석용)
 */
@Repository
public interface ScreenTimeSnapshotRepository extends MongoRepository<ScreenTimeSnapshot, String> {

    /**
     * 특정 사용자의 특정 날짜 Snapshot 조회
     * 시간대별 스크린타임 패턴 분석용
     */
    List<ScreenTimeSnapshot> findByUserIdAndDateOrderByRecordedAtDesc(
        Long userId, LocalDate date
    );

    /**
     * 특정 그룹, 사용자의 특정 기간 내 Snapshot 조회
     * 데이터 검증 및 이상 패턴 감지용
     */
    List<ScreenTimeSnapshot> findByGroupIdAndUserIdAndDateBetweenOrderByRecordedAtDesc(
        Long groupId, Long userId, LocalDate startDate, LocalDate endDate
    );

    /**
     * 특정 시간 범위 내 Snapshot 조회
     * 실시간 분석용
     */
    List<ScreenTimeSnapshot> findByUserIdAndRecordedAtBetween(
        Long userId, LocalDateTime startTime, LocalDateTime endTime
    );

    /**
     * 특정 사용자의 최근 Snapshot 조회
     * 최신 스크린타임 확인용
     */
    List<ScreenTimeSnapshot> findTop10ByUserIdOrderByRecordedAtDesc(Long userId);

    List<ScreenTimeSnapshot> findByGroupIdAndUserIdAndDateBetweenOrderByRecordedAtAsc(
            Long groupId,
            Long userId,
            String startDate,
            String endDate
    );

}
