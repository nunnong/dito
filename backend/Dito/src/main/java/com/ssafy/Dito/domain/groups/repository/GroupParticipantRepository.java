package com.ssafy.Dito.domain.groups.repository;

import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import com.ssafy.Dito.domain.groups.entity.GroupParticipant;
import com.ssafy.Dito.domain.groups.entity.GroupParticipant.GroupParticipantId;
import com.ssafy.Dito.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupParticipantRepository extends JpaRepository<GroupParticipant, GroupParticipantId> {

    boolean existsByIdUserAndIdGroup(User user, GroupChallenge group);

    Optional<GroupParticipant> findByIdUserAndIdGroup(User user, GroupChallenge group);
}
