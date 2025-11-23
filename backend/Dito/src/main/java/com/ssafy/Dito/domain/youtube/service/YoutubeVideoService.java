package com.ssafy.Dito.domain.youtube.service;

import com.ssafy.Dito.domain.youtube.entity.YoutubeVideo;
import com.ssafy.Dito.domain.youtube.repository.YoutubeVideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeVideoService {

    private final YoutubeVideoRepository youtubeVideoRepository;

    /**
     * Get YouTube video ID by channel and title
     * @param channel Channel name
     * @param title Video title
     * @return Optional video ID
     */
    @Transactional(readOnly = true)
    public Optional<Long> getVideoIdByChannelAndTitle(String channel, String title) {
        return youtubeVideoRepository.findIdByChannelAndTitle(channel, title);
    }

    /**
     * Get YouTube videos by ID list (batch query)
     * Returns a map of video_id -> YoutubeVideo for efficient lookup
     * @param videoIds List of video IDs
     * @return Map of video ID to YoutubeVideo entity
     */
    @Transactional(readOnly = true)
    public Map<Long, YoutubeVideo> getVideosByIds(List<Long> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return Map.of();
        }

        List<YoutubeVideo> videos = youtubeVideoRepository.findAllByIdIn(videoIds);
        return videos.stream()
            .collect(Collectors.toMap(
                YoutubeVideo::getId,
                video -> video
            ));
    }

    /**
     * Get YouTube video by ID
     * @param videoId Video ID
     * @return Optional YoutubeVideo
     */
    @Transactional(readOnly = true)
    public Optional<YoutubeVideo> getVideoById(Long videoId) {
        return youtubeVideoRepository.findById(videoId);
    }
}
