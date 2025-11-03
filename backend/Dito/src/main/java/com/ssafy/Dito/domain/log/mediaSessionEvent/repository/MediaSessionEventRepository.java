package com.ssafy.Dito.domain.log.mediaSessionEvent.repository;

import com.ssafy.Dito.domain.log.mediaSessionEvent.entity.MediaSessionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaSessionEventRepository extends JpaRepository<MediaSessionEvent, Long> {

}
