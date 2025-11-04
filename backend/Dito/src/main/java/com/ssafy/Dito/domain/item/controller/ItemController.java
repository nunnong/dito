package com.ssafy.Dito.domain.item.controller;

import com.ssafy.Dito.domain.item.dto.response.ItemRes;
import com.ssafy.Dito.domain.item.entity.Type;
import com.ssafy.Dito.domain.item.service.ItemService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/item")
public class ItemController {

    private final ItemService itemService;

    //상점 의상 조회
    @GetMapping("/shop/costume")
    public ResponseEntity<PageResult<ItemRes>> getShopCostume(
        @RequestParam Type type,
        @RequestParam long page_number
    ) {
        Page<ItemRes> res = itemService.getShopCostume(type, page_number);
        return ApiResponse.ok(res);
    }

    // 상점 배경 조회

    // 상점 아이탬 구매
}
