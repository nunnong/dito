-- ==============================================
-- V12__create_youtube_video_cache_table.sql
-- ==============================================
-- 목적: YouTube 영상 정보를 캐싱하는 테이블 생성
-- 작성일: 2025-11-23
-- 변경내용:
--   - heartbeat에서 수신한 YouTube 영상 정보를 PostgreSQL에 캐싱
--   - thumbnail을 Base64 형태로 저장하여 빠른 조회 지원
--   - channel + title의 unique 제약으로 중복 방지
-- ==============================================

-- 1. Create youtube_video table
CREATE TABLE IF NOT EXISTS youtube_video (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    channel TEXT NOT NULL,
    thumbnail_base64 TEXT,
    app_package VARCHAR(255),
    platform VARCHAR(50),
    video_type VARCHAR(100),
    keywords TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Unique constraint: same channel cannot have duplicate video titles
    CONSTRAINT uk_youtube_video_channel_title UNIQUE (channel, title)
);

-- 2. Add comments to columns
COMMENT ON TABLE youtube_video IS 'YouTube 영상 캐싱 테이블';
COMMENT ON COLUMN youtube_video.id IS '영상 고유 ID (자동 증가)';
COMMENT ON COLUMN youtube_video.title IS '영상 제목';
COMMENT ON COLUMN youtube_video.channel IS '채널명';
COMMENT ON COLUMN youtube_video.thumbnail_base64 IS '썸네일 이미지 (Base64 인코딩)';
COMMENT ON COLUMN youtube_video.app_package IS '앱 패키지명 (예: com.google.android.youtube)';
COMMENT ON COLUMN youtube_video.platform IS '플랫폼명 (예: YouTube, Netflix)';
COMMENT ON COLUMN youtube_video.video_type IS '영상 타입 (예: educational, entertainment)';
COMMENT ON COLUMN youtube_video.keywords IS '키워드 목록 (향후 분류용)';
COMMENT ON COLUMN youtube_video.created_at IS '최초 저장 시각';
COMMENT ON COLUMN youtube_video.updated_at IS '마지막 업데이트 시각';

-- 3. Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_youtube_video_channel ON youtube_video(channel);
CREATE INDEX IF NOT EXISTS idx_youtube_video_platform ON youtube_video(platform);
CREATE INDEX IF NOT EXISTS idx_youtube_video_created_at ON youtube_video(created_at DESC);

-- ==============================================
-- Migration Notes:
-- - heartbeat에서 받은 MediaSession 데이터를 PostgreSQL에 캐싱합니다.
-- - channel + title 조합으로 중복을 방지합니다 (동일 채널의 동일 제목 영상은 1번만 저장).
-- - thumbnail은 Base64 형태로 저장되어 별도 변환 없이 바로 사용 가능합니다.
-- ==============================================
