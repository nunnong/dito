package com.ssafy.Dito.domain.log.appUsageEvent.service;

import com.ssafy.Dito.domain.log.appUsageEvent.dto.request.AppUsageEventBatchReq;
import com.ssafy.Dito.domain.log.appUsageEvent.dto.response.AppUsageEventRes;
import com.ssafy.Dito.domain.log.appUsageEvent.entity.AppUsageEvent;
import com.ssafy.Dito.domain.log.appUsageEvent.repository.AppUsageEventRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppUsageEventService {

    private final UserRepository userRepository;
    private final AppUsageEventRepository appUsageEventRepository;

    @Transactional
    public AppUsageEventRes saveAppUsageEvent(AppUsageEventBatchReq req) {
        long userId = JwtAuthentication.getUserId();
        User user = userRepository.getById(userId);

        List<AppUsageEvent> entities = req.appUsageEvents().stream()
            .map(e -> AppUsageEvent.of(e, user))
            .collect(Collectors.toList());

        if (!entities.isEmpty()) {
            appUsageEventRepository.saveAll(entities);
        }

        return new AppUsageEventRes(entities.size());
    }
}
