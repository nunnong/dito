package com.ssafy.Dito.domain.user.userItem.controller;

import com.ssafy.Dito.domain.item.entity.Type;
import com.ssafy.Dito.domain.user.userItem.dto.request.EquipReq;
import com.ssafy.Dito.domain.user.userItem.dto.response.ClosetRes;
import com.ssafy.Dito.domain.user.userItem.service.UserItemService;
import com.ssafy.Dito.global.dto.ApiResponse;
import com.ssafy.Dito.global.dto.CommonResult;
import com.ssafy.Dito.global.dto.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/item")
public class UserItemController {

    private final UserItemService userItemService;

    // 유저 옷장 조회
    @GetMapping("/closet")
    public ResponseEntity<PageResult<ClosetRes>>getUserCloset(
        @RequestParam Type type,
        @RequestParam int page_number
    ){

        Page<ClosetRes> res = userItemService.getUserCloset(type, page_number);
        return ApiResponse.ok(res);
    }

    // 유저 아이탬 착용
    @PatchMapping("/equip")
    public ResponseEntity<CommonResult> equipUserItem(
        @RequestBody EquipReq req
    ){
        userItemService.equipUserItem(req);
        return ApiResponse.ok();
    }
}
