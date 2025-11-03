package com.ssafy.Dito.domain.auth.service;

import com.ssafy.Dito.domain.auth.dto.request.SignInReq;
import com.ssafy.Dito.domain.auth.dto.response.SignInRes;
import com.ssafy.Dito.domain.auth.exception.DuplicatedPersonalIdException;
import com.ssafy.Dito.domain.auth.exception.NotFoundUserException;
import com.ssafy.Dito.domain.auth.mapper.AuthMapper;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.domain.auth.dto.request.SignUpReq;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.global.jwt.exception.UnauthorizedUserException;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import com.ssafy.Dito.global.jwt.util.JwtClaims;
import com.ssafy.Dito.global.jwt.util.JwtUtil;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RT_PREFIX = "RefreshToken:";
    private static final String BLACKLIST_PREFIX = "BLACKLIST:";

    @Transactional
    public void signUp(SignUpReq req) {
        if(userRepository.existsByPersonalId(req.personalId())){
            throw new DuplicatedPersonalIdException();
        }
        User user = authMapper.toEntity(req);
        userRepository.save(user);
    }

    public boolean checkPersonalId(String personalId) {
        return userRepository.existsByPersonalId(personalId);
    }

    @Transactional
    public SignInRes signIn(SignInReq req) {
        User user = userRepository.getByPersonalId(req.personalId());

        if(!passwordEncoder.matches(req.password(), user.getPassword())){
            throw new NotFoundUserException();
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        redisTemplate.opsForValue().set(
                RT_PREFIX + user.getId(),
                refreshToken,
                jwtUtil.getRefreshTokenValidityMs(),
                TimeUnit.MILLISECONDS
        );

        // FCM 토큰 업데이트

        return new SignInRes(accessToken, refreshToken);
    }

    @Transactional
    public void logout(String accessToken) {
        long userId = JwtAuthentication.getUserId();

        deleteAccessToken(accessToken, userId);
    }

    @Transactional
    public SignInRes refresh(String refreshToken) {
        JwtClaims claims = jwtUtil.validateAndGetClaims(refreshToken);
        long userId = claims.getUserId();

        String storedRT = (String) redisTemplate.opsForValue().get(RT_PREFIX + userId);
        if (storedRT == null || !storedRT.equals(refreshToken)) {
            throw new UnauthorizedUserException();
        }

        String newAccessToken = jwtUtil.generateAccessToken(userId);
        return new SignInRes(newAccessToken, refreshToken);
    }
    @Transactional
    public void deleteUser(String accessToken) {
        long userId = JwtAuthentication.getUserId();

        deleteAccessToken(accessToken, userId);

        userRepository.deleteById(userId);
    }

    private void deleteAccessToken(String accessToken, long userId){
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        redisTemplate.delete(RT_PREFIX + userId);

        long expiration = jwtUtil.getRemainingTime(accessToken);

        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + accessToken,
                "LOGOUT",
                expiration,
                TimeUnit.MILLISECONDS
        );
    }
}
