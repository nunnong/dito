package com.ssafy.Dito.domain.weaklyGoal.scheduler;

import com.ssafy.Dito.domain.weaklyGoal.repository.WeeklyGoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyGoalScheduler {

    private final WeeklyGoalRepository weeklyGoalRepository;

    @Scheduled(cron = "59 59 23 ? * SUN", zone = "Asia/Seoul")
    public void deleteWeeklyGoals() {
        log.info("주간 목표가 삭제되었습니다.");
        weeklyGoalRepository.deleteAll();
    }
}
