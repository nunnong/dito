CREATE TABLE "app_usage_log" (
	"user_id"	INT		NOT NULL,
	"log_id"	INT		NULL,
	"app_name"	VARCHAR(100)		NOT NULL,
	"start_time"	TIMESTAMP		NOT NULL,
	"end_time"	TIMESTAMP		NOT NULL,
	"duration_seconds"	INT		NOT NULL,
	"session_count"	INT		NULL,
	"time_slot"	VARCHAR(20)		NOT NULL,
	"usage_date"	DATE		NOT NULL
);

CREATE TABLE "group" (
	"id"	INT		NOT NULL,
	"group_name"	VARCHAR(100)		NOT NULL,
	"invite_code"	CHAR(4)		NOT NULL,
	"period"	INT		NOT NULL,
	"start_date"	DATE		NULL,
	"end_date"	DATE		NULL,
	"goal_description"	TEXT		NULL,
	"penalty_description"	TEXT		NULL,
	"status"	VARCHAR(20)	DEFAULT 'pending'	NOT NULL,
	"total_bet_coins"	INT		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

CREATE TABLE "mission" (
	"id"	INT		NOT NULL,
	"user_id"	INT		NOT NULL,
	"mission_type"	VARCHAR(20)		NOT NULL,
	"mission_text"	TEXT		NOT NULL,
	"coin_reward"	INT		NOT NULL,
	"trigger_time"	TIMESTAMP		NOT NULL,
	"duration_seconds"	INT		NOT NULL,
	"target_app"	VARCHAR(100)		NOT NULL,
	"stat_change_self_care"	INT		NOT NULL,
	"stat_change_focus"	INT		NOT NULL,
	"stat_change_sleep"	INT		NOT NULL,
	"status"	VARCHAR(20)		NOT NULL,
	"prompt"	TEXT		NOT NULL
);

CREATE TABLE "status" (
	"id"	INT		NOT NULL,
	"user_id"	INT		NOT NULL,
	"self_care_stat"	INT	DEFAULT 0	NULL,
	"focus_stat"	INT	DEFAULT 0	NULL,
	"sleep_stat"	INT	DEFAULT 0	NULL,
	"total_statI"	INT	DEFAULT 0	NULL
);

CREATE TABLE "item" (
	"id"	INT		NOT NULL,
	"type"	VARCHAR(20)		NOT NULL,
	"name"	VARCHAR(100)		NOT NULL,
	"price"	INT		NOT NULL,
	"img_url"	VARCHAR(500)		NULL,
	"on_sale"	BOOLEAN	DEFAULT TRUE	NULL
);

COMMENT ON COLUMN "item"."type" IS '배경, 아이템';

CREATE TABLE "user" (
	"id"	INT		NOT NULL,
	"personal_id"	VARCHAR(255)		NOT NULL,
	"password"	VARCHAR(50)		NOT NULL,
	"nickname"	VARCHAR(50)		NOT NULL,
	"birth"	DATE		NOT NULL,
	"gender"	VARCHAR(10)		NOT NULL,
	"job"	VARCHAR(255)		NULL,
	"frequency"	VARCHAR(50)	DEFAULT "NORMAL"	NOT NULL,
	"coin_balance"	INT	DEFAULT 0	NOT NULL,
	"last_login_at"	TIMESTAMP	DEFAULT CURRENT_TIMESTAMP	NOT NULL,
	"created_at"	TIMESTAMP	DEFAULT CURRENT_TIMESTAMP	NOT NULL,
	"FCM_teokn"	TEXT		NOT NULL
);

CREATE TABLE "content_cache" (
	"cache_id"	INT		NULL,
	"content_id"	VARCHAR(255)		NOT NULL,
	"title"	VARCHAR(500)		NULL,
	"channel_name"	VARCHAR(255)		NULL,
	"content_type"	VARCHAR(20)		NOT NULL,
	"education_score"	DECIMAL(3,2)		NULL,
	"addiction_score"	DECIMAL(3,2)		NULL,
	"cached_at"	TIMESTAMP		NULL,
	"expires_at"	TIMESTAMP		NOT NULL
);

CREATE TABLE "mission_result" (
	"id"	INT		NOT NULL,
	"mission_id"	INT		NOT NULL,
	"result"	VARCHAR(20)		NOT NULL,
	"completed_at"	TIMESTAMP		NULL
);

CREATE TABLE "group_participant" (
	"user_id"	INT		NOT NULL,
	"group_id"	INT		NOT NULL,
	"role"	VARCHAR(10)		NOT NULL,
	"bet_coins"	INT		NOT NULL,
	"rank"	INT		NOT NULL,
	"avg_screen_time"	DECIMAL(5,2)		NOT NULL,
	"joined_at"	TIMESTAMP		NOT NULL
);

CREATE TABLE "weekly_goal" (
	"id"	INT		NOT NULL,
	"user_id"	INT		NOT NULL,
	"goal"	TEXT	DEFAULT 목표를 설정해주세요.	NOT NULL,
	"start_at"	TIMESTAMP		NOT NULL,
	"is_active"	boolean	DEFAULT FALSE	NULL
);

CREATE TABLE "user_item" (
	"item_id"	INT		NOT NULL,
	"user_id"	INT		NOT NULL,
	"purchased_at"	TIMESTAMP		NULL,
	"is_equipped"	BOOLEAN	DEFAULT FALSE	NULL
);

ALTER TABLE "group" ADD CONSTRAINT "PK_GROUP" PRIMARY KEY (
	"id"
);

ALTER TABLE "mission" ADD CONSTRAINT "PK_MISSION" PRIMARY KEY (
	"id"
);

ALTER TABLE "status" ADD CONSTRAINT "PK_STATUS" PRIMARY KEY (
	"id"
);

ALTER TABLE "item" ADD CONSTRAINT "PK_ITEM" PRIMARY KEY (
	"id"
);

ALTER TABLE "user" ADD CONSTRAINT "PK_USER" PRIMARY KEY (
	"id"
);

ALTER TABLE "mission_result" ADD CONSTRAINT "PK_MISSION_RESULT" PRIMARY KEY (
	"id"
);

ALTER TABLE "group_participant" ADD CONSTRAINT "PK_GROUP_PARTICIPANT" PRIMARY KEY (
	"user_id",
	"group_id"
);

ALTER TABLE "weekly_goal" ADD CONSTRAINT "PK_WEEKLY_GOAL" PRIMARY KEY (
	"id"
);

ALTER TABLE "user_item" ADD CONSTRAINT "PK_USER_ITEM" PRIMARY KEY (
	"item_id",
	"user_id"
);

ALTER TABLE "group_participant" ADD CONSTRAINT "FK_user_TO_group_participant_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "user" (
	"id"
);

ALTER TABLE "group_participant" ADD CONSTRAINT "FK_group_TO_group_participant_1" FOREIGN KEY (
	"group_id"
)
REFERENCES "group" (
	"id"
);

ALTER TABLE "user_item" ADD CONSTRAINT "FK_item_TO_user_item_1" FOREIGN KEY (
	"item_id"
)
REFERENCES "item" (
	"id"
);

ALTER TABLE "user_item" ADD CONSTRAINT "FK_user_TO_user_item_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "user" (
	"id"
);

