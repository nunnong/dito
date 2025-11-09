package com.ssafy.Dito.domain.user.userItem.service;

import com.ssafy.Dito.domain.item.entity.Item;
import com.ssafy.Dito.domain.item.entity.Type;
import com.ssafy.Dito.domain.item.repository.ItemRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.domain.user.userItem.dto.request.EquipReq;
import com.ssafy.Dito.domain.user.userItem.dto.response.ClosetRes;
import com.ssafy.Dito.domain.user.userItem.entity.UserItem;
import com.ssafy.Dito.domain.user.userItem.exception.AlreadyEquippedItemException;
import com.ssafy.Dito.domain.user.userItem.repository.UserItemQueryRepository;
import com.ssafy.Dito.domain.user.userItem.repository.UserItemRepository;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserItemService {

    private final UserItemQueryRepository userItemQueryRepository;
    private final UserItemRepository userItemRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ClosetRes> getUserCloset(Type type, long pageNumber) {
        long userId = JwtAuthentication.getUserId();
        Page<ClosetRes> page = userItemQueryRepository.getUserCloset(userId, type, pageNumber);

        // COSTUME 아닐 땐 그대로 리턴
        if (type != Type.COSTUME) {
            return page;
        }

        return page.map(closet -> new ClosetRes(
                closet.itemId(),
                closet.name(),
                addSuffixToImageUrl(closet.imageUrl(), "_4"),
                closet.isEquipped()
        ));
    }

    private String addSuffixToImageUrl(String url, String suffix) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        int lastDotIndex = url.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return url;
        }
        return url.substring(0, lastDotIndex) + suffix + url.substring(lastDotIndex);
    }

    @Transactional
    public void equipUserItem(EquipReq req) {
        long userId = JwtAuthentication.getUserId();

        UserItem toEquip = userItemRepository.getByIdUserIdAndIdItemId(userId,req.itemId());

        if (toEquip.isEquipped()) {
            throw new AlreadyEquippedItemException();
        }

        UserItem currentEquip = userItemQueryRepository.getEquippedItem(userId, toEquip.getId().getItem().getType());

        toEquip.updateEquipStatus(toEquip.isEquipped());
        currentEquip.updateEquipStatus(currentEquip.isEquipped());
    }
}