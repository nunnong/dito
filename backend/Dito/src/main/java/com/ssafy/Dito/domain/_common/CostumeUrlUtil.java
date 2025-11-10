package com.ssafy.Dito.domain._common;

import com.ssafy.Dito.domain.status.entity.Status;
import com.ssafy.Dito.domain.status.repository.StatusRepository;

import com.ssafy.Dito.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CostumeUrlUtil {

    private final UserRepository userRepository;
    private final StatusRepository statusRepository;

    public String getCostumeUrl(String costumeUrl, long userId){

        Status status = statusRepository.getByUserId(userId);

        // 점수기준 1~5 뭘 붙일지

        return null;
    }
}
