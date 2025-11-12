package com.ssafy.Dito.domain._common;

import com.ssafy.Dito.domain.status.entity.Status;
import com.ssafy.Dito.domain.status.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CostumeUrlUtil {

    private final StatusRepository statusRepository;

    public String getCostumeUrl(String costumeUrl, long userId, boolean isOnlyThree){

        Status status = statusRepository.getByUserId(userId);

        int score = status.getTotalStat();

        String num = isOnlyThree ? "_3" : "_" + Math.min(score / 20 + 1, 5);

        if (costumeUrl.contains(num + ".")) return costumeUrl;

        int idx = costumeUrl.lastIndexOf(".");

        return idx == -1
            ? costumeUrl
            : costumeUrl.substring(0, idx) + num + costumeUrl.substring(idx);
    }
}
