package com.ssafy.Dito.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB Configuration
 * - Enables MongoDB auditing for automatic timestamp fields
 * - Configures MongoDB repositories base package
 * - MongoTemplate bean is auto-created by Spring Boot
 */
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.ssafy.Dito.domain.log")
public class MongoConfig {
    // Auto-configuration from application.yml
    // MongoTemplate bean will be auto-created by Spring Boot
}