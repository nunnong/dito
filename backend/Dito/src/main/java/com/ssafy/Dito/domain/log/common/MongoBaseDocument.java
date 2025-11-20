package com.ssafy.Dito.domain.log.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Base document class for all MongoDB documents
 * Provides common fields: id, createdAt, updatedAt
 */
@Getter
@Setter
public abstract class MongoBaseDocument {

    @Id
    private String id;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}