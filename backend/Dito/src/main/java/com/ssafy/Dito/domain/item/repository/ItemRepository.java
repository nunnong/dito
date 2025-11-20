package com.ssafy.Dito.domain.item.repository;

import com.ssafy.Dito.domain.auth.exception.NotFoundUserException;
import com.ssafy.Dito.domain.item.entity.Item;
import com.ssafy.Dito.domain.item.exception.NotFoundItemException;
import com.ssafy.Dito.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findById(long id);

    default Item getById(long id) {
        return findById(id).orElseThrow(NotFoundItemException::new);
    }

}
