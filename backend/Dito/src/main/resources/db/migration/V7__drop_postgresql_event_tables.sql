-- ==============================================
-- V7__drop_postgresql_event_tables.sql
-- ==============================================
-- 목적: PostgreSQL 이벤트 로그 테이블 제거 (MongoDB로 마이그레이션 완료)
-- 작성일: 2025-01-04
-- 변경내용:
--   - app_usage_event 테이블 삭제 (MongoDB app_usage_logs 컬렉션으로 대체)
--   - media_session_event 테이블 삭제 (MongoDB media_session_events 컬렉션으로 대체)
--   - 관련 Foreign Key, Index는 CASCADE로 자동 삭제됨
-- ==============================================

-- 1. Drop app_usage_event table
-- Foreign key 'fk_user_to_app_usage_event' will be automatically dropped
-- Indexes 'idx_app_usage_event_user_id' and 'idx_app_usage_event_event_date' will be automatically dropped
DROP TABLE IF EXISTS app_usage_event CASCADE;

-- 2. Drop media_session_event table
-- Foreign key 'fk_user_to_media_session_event' will be automatically dropped
-- Indexes 'idx_media_session_event_user_id' and 'idx_media_session_event_event_date' will be automatically dropped
DROP TABLE IF EXISTS media_session_event CASCADE;

-- ==============================================
-- Migration Notes:
-- - 로그 데이터는 이제 MongoDB에만 저장됩니다
-- - Spring 서비스는 MongoRepository를 사용합니다
-- - API 엔드포인트는 변경 없이 유지됩니다
-- ==============================================
