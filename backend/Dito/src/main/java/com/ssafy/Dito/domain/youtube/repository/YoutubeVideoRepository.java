package com.ssafy.Dito.domain.youtube.repository;

import com.ssafy.Dito.domain.youtube.entity.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YoutubeVideoRepository extends JpaRepository<YoutubeVideo, Long> {

    /**
     * Find YouTube video by channel and title
     * @param channel Channel name
     * @param title Video title
     * @return Optional YoutubeVideo
     */
    Optional<YoutubeVideo> findByChannelAndTitle(String channel, String title);

    /**
     * Check if YouTube video exists by channel and title
     * @param channel Channel name
     * @param title Video title
     * @return true if exists, false otherwise
     */
    boolean existsByChannelAndTitle(String channel, String title);

    /**
     * Get YouTube video by channel and title
     * Throws exception if not found
     * @param channel Channel name
     * @param title Video title
     * @return YoutubeVideo
     */
    default YoutubeVideo getByChannelAndTitle(String channel, String title) {
        return findByChannelAndTitle(channel, title)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("YouTube video not found: channel=%s, title=%s", channel, title)));
    }

    /**
     * Find YouTube video ID by channel and title
     * Used for linking with MongoDB MediaSession
     * @param channel Channel name
     * @param title Video title
     * @return Optional video ID
     */
    @Query("SELECT v.id FROM YoutubeVideo v WHERE v.channel = :channel AND v.title = :title")
    Optional<Long> findIdByChannelAndTitle(@Param("channel") String channel, @Param("title") String title);

    /**
     * Find all YouTube videos by ID list (batch query)
     * Used for enriching MediaSessions with video information
     * @param ids List of video IDs
     * @return List of YoutubeVideo entities
     */
    List<YoutubeVideo> findAllByIdIn(List<Long> ids);
}
