package com.ssafy.Dito.domain.user.service;

import com.ssafy.Dito.domain.user.dto.request.FrequencyReq;
import com.ssafy.Dito.domain.user.dto.request.NicknameReq;
import com.ssafy.Dito.domain.user.dto.response.ProfileRes;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.exception.DuplicatedNicknameException;
import com.ssafy.Dito.domain.user.repository.UserQueryRepository;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;

    public ProfileRes getProfile() {
        long userId = JwtAuthentication.getUserId();
        return userQueryRepository.getProfile(userId);

    }

    @Transactional
    public void updateNickname(NicknameReq req) {
        long userId = JwtAuthentication.getUserId();
        User user = userRepository.getById(userId);

        if(user.getNickname().equals(req.nickname())){
            throw new DuplicatedNicknameException();
        }

        user.updateNickname(req);

        userRepository.save(user);
    }

    @Transactional
    public void updateFrequency(FrequencyReq req) {
        long userId = JwtAuthentication.getUserId();
        User user = userRepository.getById(userId);

        user.updateFrequency(req);

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser() {
        long userid = JwtAuthentication.getUserId();
        userRepository.deleteById(userid);
    }
}
