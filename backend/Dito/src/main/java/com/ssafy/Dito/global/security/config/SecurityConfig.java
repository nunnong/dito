package com.ssafy.Dito.global.security.config;

import com.ssafy.Dito.global.jwt.filter.JwtAuthenticationFilter;
import com.ssafy.Dito.global.jwt.util.JwtUtil;
import com.ssafy.Dito.global.security.environment.SecurityProperties;
import com.ssafy.Dito.global.security.filter.ApiKeyAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecurityProperties securityProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 기존 JWT 필터
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil, redisTemplate);

        // 새로운 API Key 필터
        ApiKeyAuthFilter apiKeyFilter = new ApiKeyAuthFilter(securityProperties);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/actuator/**",
                                "/fcm/send",  // API Key 인증 (permitAll로 필터에서 처리)
                                "/weekly-goal/user",
                                "/mission/user",
                                "/mission-result"
                        ).permitAll()
                        .anyRequest().authenticated()  // 나머지 API는 JWT 인증 필요
                )
                // 필터 순서: ApiKeyFilter → JwtFilter → UsernamePasswordAuthenticationFilter
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
