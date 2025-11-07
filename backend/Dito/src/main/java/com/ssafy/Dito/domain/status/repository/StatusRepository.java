package com.ssafy.Dito.domain.status.repository;


import com.ssafy.Dito.domain.auth.exception.NotFoundUserException;
import com.ssafy.Dito.domain.status.entity.Status;
import com.ssafy.Dito.domain.status.exception.NotFoundUserStatusException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Integer> {

    Optional<Status> findById(long id);

    default Status getById(long id) {
        return findById(id).orElseThrow(NotFoundUserStatusException::new);
    }

    Optional<Status> findByUser_Id(Long userId);

    default Status getByUserId(Long userId) {
        return findByUser_Id(userId).orElseThrow(NotFoundUserStatusException::new);
    }
}
