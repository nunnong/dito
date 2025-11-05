package com.ssafy.Dito.global.security.filter;

import com.ssafy.Dito.global.security.environment.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API Key 인증 필터
 * /fcm/send 엔드포인트에 대한 X-API-Key 헤더 검증
 */
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        // /fcm/send 경로에만 이 필터 적용
        return !path.equals("/fcm/send")
            && !path.equals("/weekly-goal/user")
            && !path.equals("/mission/user")
            && !path.equals("/mission-result");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");
        String expectedKey = securityProperties.internalApi().key();

        // API Key 검증
        if (apiKey == null || !apiKey.equals(expectedKey)) {
            log.warn("Invalid or missing API Key for request to {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"Invalid or missing API Key\"}");
            return;
        }

        log.debug("API Key validation successful for {}", request.getRequestURI());

        // 검증 성공 - 다음 필터로
        filterChain.doFilter(request, response);
    }
}
