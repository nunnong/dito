package com.ssafy.Dito.domain.item.entity;

import com.ssafy.Dito.domain._common.IdentifiableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends IdentifiableEntity {

    @Column(name = "type", length = 20, nullable = false)
    @Comment("아이템 타입")
    private String type;

    @Column(name = "name", length = 100, nullable = false)
    @Comment("아이템 이름")
    private String name;

    @Column(name = "price", nullable = false)
    @Comment("아이템 가격")
    private int price;

    @Column(name = "img_url", length = 255, nullable = true)
    @Comment("아이템 이미지 URL")
    private String imgUrl;

    @Column(name = "on_sale", nullable = false)
    @Comment("판매 여부")
    private boolean onSale;

    private Item(String type, String name, int price, String imgUrl, boolean onSale) {
        this.type = type;
        this.name = name;
        this.price = price;
        this.imgUrl = imgUrl;
        this.onSale = onSale;
    }
}
