package com.ssafy.Dito.domain.groups.repository;

import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupChallengeRepository extends JpaRepository<GroupChallenge, Long> {

    boolean existsByInviteCode(String inviteCode);
}
