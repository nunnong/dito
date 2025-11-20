-- Dito 프로젝트 초기 스키마 (ERD 기반)

-- user 테이블
CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    personal_id VARCHAR(255) NOT NULL,
    password VARCHAR(50) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    job VARCHAR(255) NULL,
    frequency VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    coin_balance INT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fcm_token TEXT NOT NULL
);

-- group 테이블
CREATE TABLE "group" (
    id BIGSERIAL PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL,
    invite_code VARCHAR(4) NOT NULL UNIQUE,
    period INT NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    goal_description TEXT NULL,
    penalty_description TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    total_bet_coins INT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- app_usage_log 테이블
CREATE TABLE app_usage_log (
    log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    app_name VARCHAR(100) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration_seconds INT NOT NULL,
    session_count INT NULL,
    time_slot VARCHAR(20) NOT NULL,
    usage_date DATE NOT NULL
);

-- mission 테이블
CREATE TABLE mission (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mission_type VARCHAR(20) NOT NULL,
    mission_text TEXT NOT NULL,
    coin_reward INT NOT NULL,
    trigger_time TIMESTAMP NOT NULL,
    duration_seconds INT NOT NULL,
    target_app VARCHAR(100) NOT NULL,
    stat_change_self_care INT NOT NULL,
    stat_change_focus INT NOT NULL,
    stat_change_sleep INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    prompt TEXT NOT NULL
);

-- status 테이블
CREATE TABLE status (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    self_care_stat INT NULL DEFAULT 0,
    focus_stat INT NULL DEFAULT 0,
    sleep_stat INT NULL DEFAULT 0,
    total_stat INT NULL DEFAULT 0
);

-- item 테이블
CREATE TABLE item (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    price INT NOT NULL,
    img_url VARCHAR(500) NULL,
    on_sale BOOLEAN NULL DEFAULT TRUE
);

COMMENT ON COLUMN item.type IS '배경, 아이템';

-- content_cache 테이블
CREATE TABLE content_cache (
    cache_id BIGSERIAL PRIMARY KEY,
    content_id VARCHAR(255) NOT NULL,
    title VARCHAR(500) NULL,
    channel_name VARCHAR(255) NULL,
    content_type VARCHAR(20) NOT NULL,
    education_score DECIMAL(3,2) NULL,
    addiction_score DECIMAL(3,2) NULL,
    cached_at TIMESTAMP NULL,
    expires_at TIMESTAMP NOT NULL
);

-- mission_result 테이블
CREATE TABLE mission_result (
    id BIGSERIAL PRIMARY KEY,
    mission_id BIGINT NOT NULL,
    result VARCHAR(20) NOT NULL,
    completed_at TIMESTAMP NULL
);

-- group_participant 테이블
CREATE TABLE group_participant (
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    role VARCHAR(10) NOT NULL,
    bet_coins INT NOT NULL,
    rank INT NOT NULL,
    avg_screen_time DECIMAL(5,2) NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, group_id)
);

-- weekly_goal 테이블
CREATE TABLE weekly_goal (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal TEXT NOT NULL DEFAULT '목표를 설정해주세요.',
    start_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NULL DEFAULT FALSE
);

-- user_item 테이블
CREATE TABLE user_item (
    item_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    purchased_at TIMESTAMP NULL,
    is_equipped BOOLEAN NULL DEFAULT FALSE,
    PRIMARY KEY (item_id, user_id)
);

-- Foreign Keys
ALTER TABLE group_participant ADD CONSTRAINT fk_user_to_group_participant_1
    FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE group_participant ADD CONSTRAINT fk_group_to_group_participant_1
    FOREIGN KEY (group_id) REFERENCES "group" (id);

ALTER TABLE user_item ADD CONSTRAINT fk_item_to_user_item_1
    FOREIGN KEY (item_id) REFERENCES item (id);

ALTER TABLE user_item ADD CONSTRAINT fk_user_to_user_item_1
    FOREIGN KEY (user_id) REFERENCES "user" (id);

-- Indexes for performance
CREATE INDEX idx_group_invite_code ON "group"(invite_code);
CREATE INDEX idx_group_status ON "group"(status);
CREATE INDEX idx_group_created_at ON "group"(created_at DESC);
CREATE INDEX idx_app_usage_log_user_id ON app_usage_log(user_id);
CREATE INDEX idx_app_usage_log_usage_date ON app_usage_log(usage_date);
CREATE INDEX idx_mission_user_id ON mission(user_id);
CREATE INDEX idx_mission_status ON mission(status);
