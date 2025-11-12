package com.ssafy.Dito.domain.groups.repository;

import com.ssafy.Dito.domain.auth.exception.NotFoundUserException;
import com.ssafy.Dito.domain.groups.entity.GroupChallenge;
import com.ssafy.Dito.domain.groups.exception.NotFoundGroupChallengeException;
import com.ssafy.Dito.domain.user.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupChallengeRepository extends JpaRepository<GroupChallenge, Long> {

    boolean existsByInviteCode(String inviteCode);

    Optional<GroupChallenge> findByInviteCode(String inviteCode);

    List<GroupChallenge> findByStatusAndEndDateBefore(String status, LocalDate date);

    Optional<GroupChallenge> findById(long id);

    default GroupChallenge getById(long id){
        return findById(id).orElseThrow(NotFoundGroupChallengeException::new);
    }
}
