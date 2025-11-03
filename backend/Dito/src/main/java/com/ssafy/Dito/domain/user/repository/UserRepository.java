package com.ssafy.Dito.domain.user.repository;

import com.ssafy.Dito.domain.auth.exception.NotFoundUserException;
import com.ssafy.Dito.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPersonalId(String personalId);

    default User getByPersonalId(String personalId) {
        return findByPersonalId(personalId).orElseThrow(NotFoundUserException::new);
    }

    boolean existsByPersonalId(String personalId);


    Optional<User> findById(long id);

    default User getById(long id) {
        return findById(id).orElseThrow(NotFoundUserException::new);
    }
}