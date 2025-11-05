package com.ssafy.Dito.domain.user.userItem.repository;

import com.ssafy.Dito.domain.auth.exception.NotFoundUserException;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.userItem.entity.UserItem;
import com.ssafy.Dito.domain.user.userItem.entity.UserItem.UserItemId;
import com.ssafy.Dito.domain.user.userItem.exception.NotFoundUserItemException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, UserItemId> {

    List<UserItem> findByIdUserAndIsEquipped(User user, boolean isEquipped);

    Optional<UserItem> findByIdUserIdAndIdItemId(long userId, long itemId);

    default UserItem getByIdUserIdAndIdItemId(long userId, long itemId) {
        return findByIdUserIdAndIdItemId(userId, itemId).orElseThrow(NotFoundUserItemException::new);
    }
}
