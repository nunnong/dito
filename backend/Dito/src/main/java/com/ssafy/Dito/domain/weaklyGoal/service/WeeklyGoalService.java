package com.ssafy.Dito.domain.weaklyGoal.service;

import com.ssafy.Dito.domain.ai.api.dto.AiReq;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.domain.weaklyGoal.dto.request.UserWeeklyGoalReq;
import com.ssafy.Dito.domain.weaklyGoal.dto.request.WeeklyGoalReq;
import com.ssafy.Dito.domain.weaklyGoal.dto.response.UserWeeklyGoalRes;
import com.ssafy.Dito.domain.weaklyGoal.entity.WeeklyGoal;
import com.ssafy.Dito.domain.weaklyGoal.repository.WeeklyGoalQueryRepository;
import com.ssafy.Dito.domain.weaklyGoal.repository.WeeklyGoalRepository;
import com.ssafy.Dito.global.exception.DuplicateException;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeeklyGoalService {

    private final UserRepository userRepository;
    private final WeeklyGoalRepository weeklyGoalRepository;
    private final WeeklyGoalQueryRepository weeklyGoalQueryRepository;

    @Transactional
    public void createWeeklyGoal(WeeklyGoalReq req) {
        long userId = JwtAuthentication.getUserId();

        if(weeklyGoalRepository.existsById(userId)){
            throw new DuplicateException("주간목표");
        }

        User user = userRepository.getById(userId);

        WeeklyGoal weeklyGoal = WeeklyGoal.of(req, user);

        weeklyGoalRepository.save(weeklyGoal);
    }

    public UserWeeklyGoalRes getUserWeeklyGoal(AiReq req) {
        return weeklyGoalQueryRepository.getUserWeeklyGoal(req.userId());
    }
}
