package com.ssafy.Dito.domain.user;

import com.ssafy.Dito.domain.auth.exception.NotFoundUserException;
import com.ssafy.Dito.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByPersonalId(String psersonalId);

    default User getByPersonalId(String personalId) {
        return findByPersonalId(personalId).orElseThrow(NotFoundUserException::new);
    }

    boolean existsByPersonalId(String personalId);
}