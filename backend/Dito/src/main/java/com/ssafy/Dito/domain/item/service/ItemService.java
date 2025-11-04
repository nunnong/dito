package com.ssafy.Dito.domain.item.service;

import com.ssafy.Dito.domain.item.dto.response.ItemRes;
import com.ssafy.Dito.domain.item.entity.Type;
import com.ssafy.Dito.domain.item.repository.ItemQueryRepository;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemQueryRepository itemQueryRepository;

    public Page<ItemRes> getShopCostume(Type type, long pageNumber) {
        long userId = JwtAuthentication.getUserId();
        return itemQueryRepository.getItemPage(userId, type, pageNumber);
    }
}
