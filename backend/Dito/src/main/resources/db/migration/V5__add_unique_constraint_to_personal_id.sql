-- ==============================================
-- V5__add_unique_constraint_to_personal_id.sql
-- 목적: user 테이블 personal_id 컬럼에 UNIQUE 제약 조건 추가
-- 작성자: 위지훈
-- ==============================================
-- 실행일시: 2025-11-03
-- 변경내용 요약:
--  - personal_id 컬럼에 UNIQUE 제약 조건 추가
-- ==============================================

ALTER TABLE "user"
    ADD CONSTRAINT uq_user_personal_id UNIQUE (personal_id);

COMMENT ON CONSTRAINT uq_user_personal_id ON "user" IS 'personal_id 중복 방지 제약 조건';
