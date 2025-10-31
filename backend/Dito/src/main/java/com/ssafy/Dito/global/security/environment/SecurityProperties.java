package com.ssafy.Dito.global.security.environment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
        JwtProperties jwt
) {

    public record JwtProperties(
            AccessProperties access,
            RefreshProperties refresh
    ) {

        public record AccessProperties(
                String secret,
                int expiration
        ) {

        }

        public record RefreshProperties(
                String secret,
                int expirationDefault
        ) {

        }
    }
}
