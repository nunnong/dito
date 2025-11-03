package com.ssafy.Dito.global.jwt.filter;

import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import com.ssafy.Dito.global.jwt.util.JwtClaims;
import com.ssafy.Dito.global.jwt.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // JWT 필터를 적용하지 않을 경로
        return method.equalsIgnoreCase("OPTIONS")
            || path.startsWith("/auth")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-resources")
            || path.startsWith("/actuator");  // Health check 등
    }


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            String isBlacklisted = (String) redisTemplate.opsForValue().get("BLACKLIST:" + token);
            if (isBlacklisted != null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            try {
                JwtClaims claims = jwtUtil.validateAndGetClaims(token);

                JwtAuthentication jwtAuthentication = JwtAuthentication.of(request, claims);
                SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
            } catch (Exception ignored) {

            }
        }
        filterChain.doFilter(request, response);
    }
}
