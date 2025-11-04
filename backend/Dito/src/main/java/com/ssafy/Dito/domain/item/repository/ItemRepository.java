package com.ssafy.Dito.domain.item.repository;

import com.ssafy.Dito.domain.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

}
