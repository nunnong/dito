package com.ssafy.Dito.domain.mission.repository;

import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.mission.entity.Status;
import com.ssafy.Dito.domain.mission.exception.NotFoundMissionException;
import com.ssafy.Dito.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long>{

    Optional<Mission> findById(long missionId);

    default Mission getById(long missionId) {
        return findById(missionId).orElseThrow(NotFoundMissionException::new);
    }

    boolean existsByUser_IdAndStatus(Long userId, Status status);

    Optional<Mission> findByUser_IdAndStatus(Long userId, Status status);
}
