package com.ssafy.Dito.domain.item.service;

import com.ssafy.Dito.domain._common.CostumeUrlUtil;
import com.ssafy.Dito.domain.item.dto.request.PurchaseItemReq;
import com.ssafy.Dito.domain.item.dto.response.ItemRes;
import com.ssafy.Dito.domain.item.dto.response.ShopItemRes;
import com.ssafy.Dito.domain.item.entity.Item;
import com.ssafy.Dito.domain.item.entity.Type;
import com.ssafy.Dito.domain.item.exception.InsufficientBalanceException;
import com.ssafy.Dito.domain.item.repository.ItemQueryRepository;
import com.ssafy.Dito.domain.item.repository.ItemRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.exception.DuplicatedNicknameException;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.domain.user.userItem.entity.UserItem;
import com.ssafy.Dito.domain.user.userItem.repository.UserItemRepository;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;
    private final ItemQueryRepository itemQueryRepository;
    private final CostumeUrlUtil costumeUrlUtil;

    @Transactional(readOnly = true)
    public Page<ShopItemRes> getShopItem(Type type, long pageNumber) {
        long userId = JwtAuthentication.getUserId();
        Page<ShopItemRes> page = itemQueryRepository.getItemPage(userId, type, pageNumber);

        return page.map(shopItemRes -> {
            List<ItemRes> modifiedItems = shopItemRes.items().stream()
                .map(item -> new ItemRes(
                    item.ItemId(),
                    item.name(),
                    item.price(),
                        type == Type.COSTUME
                            ? costumeUrlUtil.getCostumeUrl(item.imageUrl(), userId, true)
                                : item.imageUrl(),
                    item.onSale(),
                    item.isPurchased()
                ))
                .toList();

            return new ShopItemRes(shopItemRes.coin_balance(), modifiedItems);
        });
    }

    @Transactional
    public void purchaseShopItem(PurchaseItemReq req) {
        long userId = JwtAuthentication.getUserId();

        User user = userRepository.getById(userId);
        Item item = itemRepository.getById(req.itemId());

        if(user.getCoinBalance() < item.getPrice()) {
            throw new InsufficientBalanceException();
        }

        UserItem userItem = UserItem.of(user, item, false);

        user.updateCoin(item.getPrice());
        userItemRepository.save(userItem);
    }
}
