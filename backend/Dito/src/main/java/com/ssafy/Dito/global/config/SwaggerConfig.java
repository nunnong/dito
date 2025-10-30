package com.ssafy.Dito.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // JWT 보안 스키마 정의
        String jwt = "JWT";
        String userId = "X-User-Id";

        SecurityRequirement jwtRequirement = new SecurityRequirement().addList(jwt);
        SecurityRequirement userIdRequirement = new SecurityRequirement().addList(userId);

        Components components = new Components()
                .addSecuritySchemes(jwt, new SecurityScheme()
                        .name(jwt)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰 인증 (향후 구현 예정)"))
                .addSecuritySchemes(userId, new SecurityScheme()
                        .name(userId)
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description("임시 사용자 ID (개발 환경)"));

        return new OpenAPI()
                .addServersItem(new Server().url("/").description("Default Server URL"))
                .components(components)
                .addSecurityItem(userIdRequirement)  // 기본적으로 X-User-Id 사용
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Dito API Documentation")
                .description("Digital Detox Application API 명세서")
                .version("1.0.0");
    }
}
