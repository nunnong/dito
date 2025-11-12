package com.ssafy.Dito.domain.screentime.repository;

import com.ssafy.Dito.domain.screentime.document.ScreenTimeDailySummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for daily screen time summary
 * 그룹 챌린지 랭킹 조회 최적화를 위한 일별 스크린타임 집계 데이터 관리
 */
@Repository
public interface ScreenTimeDailySummaryRepository extends MongoRepository<ScreenTimeDailySummary, String> {

    /**
     * 특정 그룹, 사용자, 날짜의 Summary 조회
     * 앱에서 스크린타임 갱신 시 기존 데이터 확인용
     */
    Optional<ScreenTimeDailySummary> findByGroupIdAndUserIdAndDate(
        Long groupId, Long userId, String date
    );

    /**
     * 특정 그룹의 특정 기간 내 모든 사용자의 Summary 조회
     * 그룹 챌린지 랭킹 계산용
     */
    List<ScreenTimeDailySummary> findByGroupIdAndDateBetween(
        Long groupId, String startDate, String endDate
    );

    /**
     * 특정 그룹, 사용자의 특정 기간 내 Summary 조회
     * 개인별 스크린타임 추이 분석용
     */
    List<ScreenTimeDailySummary> findByGroupIdAndUserIdAndDateBetween(
        Long groupId, Long userId, String startDate, String endDate
    );

    /**
     * 특정 사용자의 모든 그룹에서의 특정 날짜 Summary 조회
     */
    List<ScreenTimeDailySummary> findByUserIdAndDate(Long userId, String date);

    /**
     * 특정 그룹의 모든 Summary 삭제
     * 그룹 삭제 시 사용
     */
    void deleteByGroupId(Long groupId);
}
