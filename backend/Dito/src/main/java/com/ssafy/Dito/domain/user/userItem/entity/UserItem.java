package com.ssafy.Dito.domain.user.userItem.entity;

import com.ssafy.Dito.domain.item.entity.Item;
import com.ssafy.Dito.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.sql.Timestamp;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("유저 - 아이탬")
public class UserItem {

    @EmbeddedId
    private UserItemId id;

    @Column(name = "purchased_at", nullable = false)
    @CreatedDate
    @Comment("구매 일시")
    Timestamp purchasedAt;

    @Column(name = "is_equipped", nullable = false)
    @Comment("착용 여부")
    private boolean isEquipped;

    private UserItem(User user, Item item, boolean isEquipped) {
        this.id = new UserItemId(user, item);
        this.isEquipped = false;
    }

    @Getter
    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @EqualsAndHashCode
    public static class UserItemId {

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "item_id", nullable = false)
        private Item item;
    }
}