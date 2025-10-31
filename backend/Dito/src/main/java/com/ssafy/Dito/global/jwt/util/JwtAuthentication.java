package com.ssafy.Dito.global.jwt.util;


import com.ssafy.Dito.global.jwt.exception.UnauthorizedUserException;
import io.jsonwebtoken.Claims;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.ObjectUtils;

@Slf4j
public class JwtAuthentication extends UsernamePasswordAuthenticationToken {
    public JwtAuthentication(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public static JwtAuthentication of(HttpServletRequest request, JwtClaims claims) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        JwtAuthentication jwtAuthentication = new JwtAuthentication(claims, null, authorities);
        jwtAuthentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return jwtAuthentication;
    }

    /**
     * 현재 로그인된 유저 아이디 반환.
     * 로그인이 안된 상태라면 익셉션 발생
     *
     * @return Long
     */
    public static Long getUserId() {
        return findUserId().orElseThrow(UnauthorizedUserException::new);
    }

    /**
     * 현재 로그인된 유저 아이디 반환.
     * 로그인이 안된 상태더라도 문제 없음
     *
     * @return Optional.empty(), Optional<Long>
     */
    public static Optional<Long> findUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (ObjectUtils.isEmpty(authentication)) {
            return Optional.empty();
        }

        if (authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        if (authentication instanceof JwtAuthentication jwtAuthentication) {
            JwtClaims jwtClaims = jwtAuthentication.getPrincipal();
            return Optional.of(jwtClaims.getUserId());
        }

        log.error("Authentication 타입 불일치");
        throw new UnauthorizedUserException();
    }

    @Override
    public JwtClaims getPrincipal() {
        if (super.getPrincipal() instanceof JwtClaims claims) {
            return claims;
        }
        // TODO 명시적 Exception 추가
        throw new RuntimeException();
    }

    @Override
    public WebAuthenticationDetails getDetails() {
        if (super.getDetails() instanceof WebAuthenticationDetails details) {
            return details;
        }
        // TODO 명시적 Exception 추가
        throw new RuntimeException();
    }
}
