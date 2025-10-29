package com.ssafy.Dito.domain.auth.service;

import com.ssafy.Dito.domain.auth.dto.request.SignInReq;
import com.ssafy.Dito.domain.auth.dto.response.SignInRes;
import com.ssafy.Dito.domain.auth.exception.NotFoundUserException;
import com.ssafy.Dito.domain.auth.mapper.AuthMapper;
import com.ssafy.Dito.domain.user.UserRepository;
import com.ssafy.Dito.domain.auth.dto.request.SignUpReq;
import com.ssafy.Dito.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthMapper authMapper;

    @Transactional
    public void signUp(SignUpReq req) {
        User user = authMapper.toEntity(req);
        userRepository.save(user);
    }

    public boolean checkPersonalId(String personalId) {
        return userRepository.existsByPersonalId(personalId);
    }

    public SignInRes signIn(SignInReq req) {
        User user = userRepository.getByPersonalId(req.personalId());
        if(!req.password().equals(user.getPassword())){
            throw new NotFoundUserException();
        }

        // 토큰 생성해서 반환
    }
}
