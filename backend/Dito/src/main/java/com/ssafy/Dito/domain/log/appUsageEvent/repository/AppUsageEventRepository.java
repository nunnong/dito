package com.ssafy.Dito.domain.log.appUsageEvent.repository;

import com.ssafy.Dito.domain.log.appUsageEvent.entity.AppUsageEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUsageEventRepository extends JpaRepository<AppUsageEvent, Long> {

}
