package com.ssafy.Dito.domain.mission.repository;

import com.ssafy.Dito.domain.mission.entity.Mission;
import com.ssafy.Dito.domain.mission.exception.NotFoundMissionException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long>{

    default Mission getById(long missionId) {
        return findById(missionId).orElseThrow(NotFoundMissionException::new);
    }
}
