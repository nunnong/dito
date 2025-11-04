package com.ssafy.Dito.domain.groups.scheduler;

import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import com.ssafy.Dito.domain.groups.repository.GroupChallengeRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupChallengeScheduler {

    private final GroupChallengeRepository groupChallengeRepository;

    /**
     * 매일 23:59에 실행되어 종료일이 지난 챌린지들을 자동으로 완료 처리합니다.
     */
    @Scheduled(cron = "0 59 23 * * *")
    @Transactional
    public void completeChallenges() {
        LocalDate today = LocalDate.now();

        log.info("챌린지 자동 종료 스케줄러 실행 - 기준일: {}", today);

        // status가 "in_progress"이고 end_date가 오늘 이전인 챌린지들 조회
        List<GroupChallenge> challengesToComplete = groupChallengeRepository
            .findByStatusAndEndDateBefore("in_progress", today.plusDays(1));

        if (challengesToComplete.isEmpty()) {
            log.info("종료할 챌린지가 없습니다.");
            return;
        }

        // 챌린지들을 완료 상태로 변경
        for (GroupChallenge challenge : challengesToComplete) {
            challenge.completeChallenge();
            log.info("챌린지 종료 처리 완료 - ID: {}, 그룹명: {}, 종료일: {}",
                challenge.getId(),
                challenge.getGroupName(),
                challenge.getEndDate());
        }

        log.info("총 {}개의 챌린지가 종료 처리되었습니다.", challengesToComplete.size());
    }
}
