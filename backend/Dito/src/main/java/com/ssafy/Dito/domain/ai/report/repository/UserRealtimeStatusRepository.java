package com.ssafy.Dito.domain.ai.report.repository;

import com.ssafy.Dito.domain.ai.report.document.UserRealtimeStatusDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRealtimeStatusRepository extends MongoRepository<UserRealtimeStatusDocument, Long> {
}
