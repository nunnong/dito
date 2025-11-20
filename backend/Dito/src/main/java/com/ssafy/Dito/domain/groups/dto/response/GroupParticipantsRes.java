package com.ssafy.Dito.domain.groups.dto.response;

import java.util.List;

public record GroupParticipantsRes(
    Long groupId,
    Integer count,
    List<ParticipantInfo> participants
) {
    public static GroupParticipantsRes of(Long groupId, List<ParticipantInfo> participants) {
        return new GroupParticipantsRes(groupId, participants.size(), participants);
    }
}
