package com.ssafy.Dito.domain.groups.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.Dito.domain._common.CostumeUrlUtil;
import com.ssafy.Dito.domain.groups.dto.response.EquippedItemInfo;
import com.ssafy.Dito.domain.groups.dto.response.GroupParticipantsRes;
import com.ssafy.Dito.domain.groups.dto.response.ParticipantInfo;
import com.ssafy.Dito.domain.groups.dto.response.QEquippedItemInfo;
import com.ssafy.Dito.domain.groups.entity.QGroupParticipant;
import com.ssafy.Dito.domain.item.entity.QItem;
import com.ssafy.Dito.domain.user.entity.QUser;
import com.ssafy.Dito.domain.user.userItem.entity.QUserItem;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupParticipantQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final CostumeUrlUtil costumeUrlUtil;

    private final QGroupParticipant groupParticipant = QGroupParticipant.groupParticipant;
    private final QUser user = QUser.user;
    private final QUserItem userItem = QUserItem.userItem;
    private final QItem item = QItem.item;

    public GroupParticipantsRes getParticipants(Long groupId) {
        // 1. 참여자 기본 정보 조회
        List<ParticipantBasicInfo> participantBasics = jpaQueryFactory
            .select(
                user.id,
                user.nickname,
                groupParticipant.role,
                groupParticipant.betCoins
            )
            .from(groupParticipant)
            .join(groupParticipant.id.user, user)
            .where(groupParticipant.id.group.id.eq(groupId))
            .fetch()
            .stream()
            .map(tuple -> new ParticipantBasicInfo(
                tuple.get(user.id),
                tuple.get(user.nickname),
                tuple.get(groupParticipant.role),
                tuple.get(groupParticipant.betCoins)
            ))
            .toList();

        // 2. 모든 참여자의 userId 추출
        List<Long> userIds = participantBasics.stream()
            .map(ParticipantBasicInfo::userId)
            .toList();

        // 3. 모든 참여자의 장착된 아이템 조회
        List<EquippedItemInfo> allEquippedItems = jpaQueryFactory
            .select(new QEquippedItemInfo(
                userItem.id.user.id.stringValue()
                    .concat("_")
                    .concat(userItem.id.item.id.stringValue()),
                userItem.id.item.id,
                userItem.id.item.type.stringValue().lower(),
                userItem.id.item.name,
                userItem.id.item.imgUrl
            ))
            .from(userItem)
            .join(userItem.id.item, item)
            .where(
                userItem.id.user.id.in(userIds),
                userItem.isEquipped.eq(true)
            )
            .fetch()
            .stream()
            .map(equipItem -> {
                // userId 추출
                Long userId = Long.parseLong(equipItem.userItemId().split("_")[0]);
                // CostumeUrlUtil을 사용하여 URL 변환
                String transformedUrl = costumeUrlUtil.getCostumeUrl(
                    equipItem.imgUrl(),
                    userId,
                    false
                );
                // 변환된 URL로 새 EquippedItemInfo 생성
                return new EquippedItemInfo(
                    equipItem.userItemId(),
                    equipItem.itemId(),
                    equipItem.type(),
                    equipItem.name(),
                    transformedUrl
                );
            })
            .toList();

        // 4. userId별로 장착 아이템 그룹화
        Map<Long, List<EquippedItemInfo>> equippedItemsByUser = allEquippedItems.stream()
            .collect(Collectors.groupingBy(equipItem -> {
                String userItemId = equipItem.userItemId();
                return Long.parseLong(userItemId.split("_")[0]);
            }));

        // 5. ParticipantInfo 생성
        List<ParticipantInfo> participants = participantBasics.stream()
            .map(basic -> ParticipantInfo.of(
                basic.userId(),
                basic.nickname(),
                basic.role(),
                basic.betCoins(),
                equippedItemsByUser.getOrDefault(basic.userId(), List.of())
            ))
            .toList();

        return GroupParticipantsRes.of(groupId, participants);
    }

    // 내부 DTO
    private record ParticipantBasicInfo(
        Long userId,
        String nickname,
        String role,
        Integer betCoins
    ) {}
}