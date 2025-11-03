-- ==============================================
-- V3__update_mission_trigger_time_nullable.sql
-- 목적: mission 테이블의 trigger_time 컬럼 NULL 허용으로 변경
-- 작성자: 위지훈
-- ==============================================
-- 실행일시: 2025-11-03
-- 변경내용 요약:
--  - trigger_time 컬럼을 NOT NULL → NULL 허용으로 변경
--  - 컬럼 주석 유지
-- ==============================================

-- 1. trigger_time 컬럼 NULL 허용으로 변경
ALTER TABLE mission
    ALTER COLUMN trigger_time DROP NOT NULL;

-- 2. 컬럼 코멘트 재적용 (명확한 문서화)
COMMENT ON COLUMN mission.trigger_time IS '미션 시작 시간';