package com.ssafy.Dito.domain.screentime.repository;

import com.ssafy.Dito.domain.screentime.document.CurrentAppUsage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CurrentAppUsage MongoDB Repository
 */
@Repository
public interface CurrentAppUsageRepository extends MongoRepository<CurrentAppUsage, String> {

    /**
     * 그룹 ID와 사용자 ID로 현재 앱 정보 조회
     */
    Optional<CurrentAppUsage> findByGroupIdAndUserId(Long groupId, Long userId);

    /**
     * 그룹 ID로 모든 참여자의 현재 앱 정보 조회
     */
    List<CurrentAppUsage> findAllByGroupId(Long groupId);
}
