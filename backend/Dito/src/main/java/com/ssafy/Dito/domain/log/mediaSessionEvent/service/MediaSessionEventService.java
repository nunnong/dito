package com.ssafy.Dito.domain.log.mediaSessionEvent.service;

import com.ssafy.Dito.domain.log.mediaSessionEvent.dto.request.MediaSessionEventBatchReq;
import com.ssafy.Dito.domain.log.mediaSessionEvent.dto.response.MediaSessionEventRes;
import com.ssafy.Dito.domain.log.mediaSessionEvent.entity.MediaSessionEvent;
import com.ssafy.Dito.domain.log.mediaSessionEvent.repository.MediaSessionEventRepository;
import com.ssafy.Dito.domain.user.entity.User;
import com.ssafy.Dito.domain.user.repository.UserRepository;
import com.ssafy.Dito.global.jwt.util.JwtAuthentication;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MediaSessionEventService {

    private final UserRepository userRepository;
    private final MediaSessionEventRepository mediaSessionEventRepository;

    @Transactional
    public MediaSessionEventRes saveMediaSessionEvent(MediaSessionEventBatchReq req) {
        long userId = JwtAuthentication.getUserId();
        User user = userRepository.getById(userId);

        List<MediaSessionEvent> entities = req.mediaSessionEvent().stream()
            .map(e -> MediaSessionEvent.of(e, user))
            .collect(Collectors.toList());

        if (!entities.isEmpty()) {
            mediaSessionEventRepository.saveAll(entities);
        }

        return new MediaSessionEventRes(entities.size());

    }
}
