package com.ssafy.Dito.domain.auth.service;

import com.ssafy.Dito.domain.auth.dto.request.SignInReq;
import com.ssafy.Dito.domain.auth.dto.response.SignInRes;
import com.ssafy.Dito.domain.auth.exception.DuplicatedPersonalIdException;
import com.ssafy.Dito.domain.auth.exception.NotFoundUserException;
import com.ssafy.Dito.domain.item.entity.Item;
import com.ssafy.Dito.domain.item.repository.ItemRepository;
import com.ssafy.Dito.domain.status.entity.Status;
import com.ssafy.Dito.domain.status.repository.StatusRepository;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.domain.auth.dto.request.SignUpReq;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.userItem.entity.UserItem;
import com.ssafy.Dito.domain.user.userItem.repository.UserItemRepository;
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
    private final StatusRepository statusRepository;
    private final UserItemRepository userItemRepository;
    private final ItemRepository itemRepository;
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

        String encodedPassword = passwordEncoder.encode(req.password());

        User user = User.of(req, encodedPassword);
        userRepository.save(user);

        Status status = Status.of(user);
        statusRepository.save(status);

        Item costume = itemRepository.getById(3);
        Item background = itemRepository.getById(14);

        UserItem defaultCostume = UserItem.of(user, costume, true);
        UserItem defaultBackground = UserItem.of(user, background, true);
        userItemRepository.save(defaultCostume);
        userItemRepository.save(defaultBackground);
    }

    public boolean checkPersonalId(String personalId) {
        return !userRepository.existsByPersonalId(personalId);
    }

    @Transactional
    public SignInRes signIn(SignInReq req) {
        User user = userRepository.getByPersonalId(req.personalId());

        if(user == null || !passwordEncoder.matches(req.password(), user.getPassword())){
            throw new NotFoundUserException();
        }

        // FCM 토큰 갱신 (Android가 Firebase에서 받은 토큰)
        if (req.fcmToken() != null && !req.fcmToken().isBlank()) {
            user.updateFcmToken(req.fcmToken());
            userRepository.save(user);
        }

        return createToken(user.getId());
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

    private SignInRes createToken(long userId){
        String accessToken = jwtUtil.generateAccessToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        redisTemplate.opsForValue().set(
            RT_PREFIX + userId,
            refreshToken,
            jwtUtil.getRefreshTokenValidityMs(),
            TimeUnit.MILLISECONDS
        );

        return new SignInRes(accessToken, refreshToken);
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
