package com.ssafy.Dito.domain.ai.report.repository;

import com.ssafy.Dito.domain.ai.report.document.UserRealtimeStatusDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRealtimeStatusRepository extends MongoRepository<UserRealtimeStatusDocument, String> {
    Optional<UserRealtimeStatusDocument> findByUserId(Long userId);
}
