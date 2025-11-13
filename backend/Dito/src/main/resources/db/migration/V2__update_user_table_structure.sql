-- ==============================================
-- V2__update_user_table_structure.sql
-- 목적: user 테이블 구조 보정 및 주석, 기본값 통일
-- 작성자: 위지훈
-- ==============================================
-- 실행일시: 2025-11-01
-- 변경내용 요약:
--  - password 길이 확장 (VARCHAR(50) → 255)
--  - job 기본값 및 NOT NULL 추가
--  - frequency 기본값 통일 ('NORMAL')
--  - fcm_token nullable로 변경
--  - 컬럼 주석 추가
-- ==============================================

-- 1. password 컬럼 길이 확장 (Bcrypt 해시 길이 대응)
ALTER TABLE "user"
ALTER COLUMN "password" TYPE VARCHAR(255);

-- 2. job 기본값 및 NOT NULL 제약 추가
ALTER TABLE "user"
    ALTER COLUMN "job" SET DEFAULT 'ETC',
ALTER COLUMN "job" SET NOT NULL;

-- 3. frequency 기본값 보정 (명시적으로 NORMAL 설정)
ALTER TABLE "user"
    ALTER COLUMN "frequency" SET DEFAULT 'NORMAL';

-- 4. last_login_at 컬럼 nullable 변경
ALTER TABLE "user"
    ALTER COLUMN last_login_at DROP NOT NULL;

-- 5. fcm_token 컬럼 nullable 변경 및 타입 통일 (TEXT → VARCHAR(255))
ALTER TABLE "user"
ALTER COLUMN "fcm_token" TYPE VARCHAR(255),
    ALTER COLUMN "fcm_token" DROP NOT NULL;

-- 6. 컬럼 코멘트 추가 (DB 문서화)
COMMENT ON COLUMN "user"."personal_id" IS '로그인용 개인 아이디';
COMMENT ON COLUMN "user"."password" IS '암호화된 비밀번호 (Bcrypt 등)';
COMMENT ON COLUMN "user"."nickname" IS '사용자 닉네임';
COMMENT ON COLUMN "user"."birth" IS '생년월일';
COMMENT ON COLUMN "user"."gender" IS '성별 (Enum: MALE, FEMALE, ETC)';
COMMENT ON COLUMN "user"."job" IS '직업 (Enum: STUDENT, WORKER, ETC)';
COMMENT ON COLUMN "user"."frequency" IS '앱 사용 빈도 (Enum: LOW, NORMAL, HIGH)';
COMMENT ON COLUMN "user"."coin_balance" IS '코인 잔액';
COMMENT ON COLUMN "user"."last_login_at" IS '마지막 로그인 시각';
COMMENT ON COLUMN "user"."created_at" IS '회원 가입일';
COMMENT ON COLUMN "user"."fcm_token" IS '로그인 시 갱신되는 FCM 토큰 (푸시 알림용)';