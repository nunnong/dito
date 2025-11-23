package com.ssafy.Dito.domain.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFeedbackItem {
    private String id;
    private String title;
    private String channel;
    private String thumbnailBase64;
    private Integer watchTimeMinutes;
}
