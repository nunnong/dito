package com.ssafy.Dito.global.environment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "externals")
public record ExternalsProperties(
        RedisProperties redis
) {
    public record RedisProperties(
            String host,
            int port,
            String password
    ) {}
}