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

        SecurityRequirement jwtRequirement = new SecurityRequirement().addList(jwt);

        Components components = new Components()
                .addSecuritySchemes(jwt, new SecurityScheme()
                        .name(jwt)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer 토큰 인증 - /auth/login에서 받은 accessToken을 입력하세요"));

        return new OpenAPI()
                .addServersItem(new Server().url("/").description("Default Server URL"))
                .components(components)
                .addSecurityItem(jwtRequirement)  // JWT를 기본 인증 방법으로 사용
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Dito API Documentation")
                .description("Digital Detox Application API 명세서")
                .version("1.0.0");
    }
}
