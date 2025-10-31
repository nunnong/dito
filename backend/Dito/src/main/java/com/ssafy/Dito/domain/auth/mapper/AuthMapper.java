package com.ssafy.Dito.domain.auth.mapper;

import com.ssafy.Dito.domain.auth.dto.request.SignUpReq;
import com.ssafy.Dito.domain.user.entity.Frequency;
import com.ssafy.Dito.domain.user.entity.Job;
import com.ssafy.Dito.domain.user.entity.User;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthMapper {

    private final PasswordEncoder passwordEncoder;

    public User toEntity(SignUpReq req){
        return User.of(
                req.personalId(),
                passwordEncoder.encode(req.password()),
                req.nickname(),
                req.birth(),
                req.gender(),
                Job.ETC,
                0,
                Frequency.NORMAL,
                null,
                Instant.now(),
                null
        );
    }
}
