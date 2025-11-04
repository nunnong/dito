package com.ssafy.Dito.domain.user.userItem.repository;

import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.userItem.entity.UserItem;
import com.ssafy.Dito.domain.user.userItem.entity.UserItem.UserItemId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, UserItemId> {

    List<UserItem> findByIdUserAndIsEquipped(User user, boolean isEquipped);
}
