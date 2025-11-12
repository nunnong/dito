-- ==============================================
-- V4__create_app_and_media_event_tables.sql
-- 목적: app_usage_event 및 media_session_event 테이블 신규 생성
-- 작성자: 위지훈
-- ==============================================
-- 실행일시: 2025-11-03
-- 변경내용 요약:
--  - 앱 사용 이벤트 로그(app_usage_event) 테이블 추가
--  - 미디어 세션 이벤트(media_session_event) 테이블 추가
-- ==============================================


-- app_usage_event 테이블 생성
CREATE TABLE app_usage_event (
                                 id BIGSERIAL PRIMARY KEY,
                                 event_id VARCHAR(50) NOT NULL UNIQUE,
                                 event_type VARCHAR(20) NOT NULL,
                                 package_name VARCHAR(100) NOT NULL,
                                 app_name VARCHAR(100),
                                 event_timestamp BIGINT NOT NULL,
                                 duration BIGINT,
                                 event_date DATE NOT NULL,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 user_id BIGINT NOT NULL,
                                 CONSTRAINT fk_user_to_app_usage_event FOREIGN KEY (user_id) REFERENCES "user" (id)
);

COMMENT ON TABLE app_usage_event IS '앱 사용 이벤트 로그';
COMMENT ON COLUMN app_usage_event.event_id IS '이벤트 식별자';
COMMENT ON COLUMN app_usage_event.event_type IS '이벤트 타입';
COMMENT ON COLUMN app_usage_event.package_name IS '패키지 이름';
COMMENT ON COLUMN app_usage_event.app_name IS '앱 이름';
COMMENT ON COLUMN app_usage_event.event_timestamp IS '이벤트 발생 시각';
COMMENT ON COLUMN app_usage_event.duration IS '사용 시간(ms)';
COMMENT ON COLUMN app_usage_event.event_date IS '이벤트 날짜';
COMMENT ON COLUMN app_usage_event.created_at IS '수집 시각';
COMMENT ON COLUMN app_usage_event.user_id IS '사용자 식별자';


-- media_session_event 테이블 생성
CREATE TABLE media_session_event (
                                     id BIGSERIAL PRIMARY KEY,
                                     event_id VARCHAR(50) NOT NULL UNIQUE,
                                     event_type VARCHAR(20) NOT NULL,
                                     package_name VARCHAR(100) NOT NULL,
                                     app_name VARCHAR(100),
                                     title VARCHAR(255),
                                     channel VARCHAR(200),
                                     event_timestamp BIGINT NOT NULL,
                                     video_duration BIGINT,
                                     watch_time BIGINT,
                                     pause_time BIGINT,
                                     event_date DATE NOT NULL,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     user_id BIGINT NOT NULL,
                                     CONSTRAINT fk_user_to_media_session_event FOREIGN KEY (user_id) REFERENCES "user" (id)
);

COMMENT ON TABLE media_session_event IS '미디어 세션 이벤트 로그';
COMMENT ON COLUMN media_session_event.event_id IS '이벤트 식별자';
COMMENT ON COLUMN media_session_event.event_type IS '이벤트 타입';
COMMENT ON COLUMN media_session_event.package_name IS '패키지 이름';
COMMENT ON COLUMN media_session_event.app_name IS '앱 이름';
COMMENT ON COLUMN media_session_event.title IS '미디어 제목';
COMMENT ON COLUMN media_session_event.channel IS '채널 이름';
COMMENT ON COLUMN media_session_event.event_timestamp IS '이벤트 발생 시각';
COMMENT ON COLUMN media_session_event.video_duration IS '비디오 전체 길이(ms)';
COMMENT ON COLUMN media_session_event.watch_time IS '시청 시간(ms)';
COMMENT ON COLUMN media_session_event.pause_time IS '일시정지 시간(ms)';
COMMENT ON COLUMN media_session_event.event_date IS '이벤트 날짜';
COMMENT ON COLUMN media_session_event.created_at IS '수집 시각';
COMMENT ON COLUMN media_session_event.user_id IS '사용자 식별자';


-- 인덱스 추가 (검색 성능 향상용)
CREATE INDEX idx_app_usage_event_user_id ON app_usage_event(user_id);
CREATE INDEX idx_app_usage_event_event_date ON app_usage_event(event_date DESC);

CREATE INDEX idx_media_session_event_user_id ON media_session_event(user_id);
CREATE INDEX idx_media_session_event_event_date ON media_session_event(event_date DESC);
