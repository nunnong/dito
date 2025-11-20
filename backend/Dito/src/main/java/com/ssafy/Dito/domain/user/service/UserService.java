package com.ssafy.Dito.domain.user.service;

import com.ssafy.Dito.domain._common.CostumeUrlUtil;
import com.ssafy.Dito.domain.user.dto.request.FrequencyReq;
import com.ssafy.Dito.domain.user.dto.request.NicknameReq;
import com.ssafy.Dito.domain.user.dto.response.MainRes;
import com.ssafy.Dito.domain.user.dto.response.ProfileRes;
import com.ssafy.Dito.domain.user.dto.response.UserInfoRes;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.exception.DuplicatedNicknameException;
import com.ssafy.Dito.domain.user.repository.UserQueryRepository;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.global.exception.PageNotFoundException;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;
    private final CostumeUrlUtil costumeUrlUtil;

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public MainRes getMainPage() {
        long userId = JwtAuthentication.getUserId();
        MainRes res = userQueryRepository.getMainPage(userId);
        String costumeUrl = costumeUrlUtil.getCostumeUrl(res.costumeUrl(), userId, false);
        return new MainRes(
                res.nickname(),
                costumeUrl,
                res.backgroundUrl(),
                res.coinBalance(),
                res.weeklyGoal(),
                res.selfCareStatus(),
                res.focusStatus(),
                res.sleepStatus()
        );
    }

    public UserInfoRes getUserInfoForAi(String personalId) {
        UserInfoRes res = userQueryRepository.getUserInfoForAi(personalId);
        if (res == null) {
            throw new PageNotFoundException("사용자를 찾을 수 없습니다.");
        }
        return res;
    }
}
