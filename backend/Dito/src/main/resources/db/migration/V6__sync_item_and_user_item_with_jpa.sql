-- ==============================================
-- V6__sync_item_and_user_item_with_jpa.sql
-- 목적: Item / UserItem 엔티티에 맞춰 스키마 정합성 보정
-- 기준 스키마: V1
-- ==============================================

-- 1) ITEM 테이블 보정
-- 1-1. img_url 길이 500 -> 255 (잘리는 데이터는 앞 255로 보정)
UPDATE item
SET img_url = LEFT(img_url, 255)
WHERE img_url IS NOT NULL AND LENGTH(img_url) > 255;

ALTER TABLE item
ALTER COLUMN img_url TYPE VARCHAR(255);

-- 1-2. on_sale NULL 보정 및 NOT NULL 설정(+기본값 유지)
UPDATE item
SET on_sale = TRUE
WHERE on_sale IS NULL;

ALTER TABLE item
    ALTER COLUMN on_sale SET NOT NULL,
ALTER COLUMN on_sale SET DEFAULT TRUE;

-- 1-3. 코멘트 정리(선택)
COMMENT ON TABLE item IS '아이템';
COMMENT ON COLUMN item.type IS '아이템 타입';
COMMENT ON COLUMN item.name IS '아이템 이름';
COMMENT ON COLUMN item.price IS '아이템 가격';
COMMENT ON COLUMN item.img_url IS '아이템 이미지 URL';
COMMENT ON COLUMN item.on_sale IS '판매 여부';

-- 2) USER_ITEM 테이블 보정
-- 2-1. purchased_at NOT NULL + DEFAULT now() (엔티티 @CreatedDate와 정합)
UPDATE user_item
SET purchased_at = NOW()
WHERE purchased_at IS NULL;

ALTER TABLE user_item
    ALTER COLUMN purchased_at SET NOT NULL,
ALTER COLUMN purchased_at SET DEFAULT CURRENT_TIMESTAMP;

-- 2-2. is_equipped NOT NULL + DEFAULT false
UPDATE user_item
SET is_equipped = FALSE
WHERE is_equipped IS NULL;

ALTER TABLE user_item
    ALTER COLUMN is_equipped SET NOT NULL,
ALTER COLUMN is_equipped SET DEFAULT FALSE;

-- 2-3. (선택) 복합 PK의 컬럼 순서를 엔티티 임베디드 키 순서(user_id, item_id)로 맞추기
--      V1은 PRIMARY KEY (item_id, user_id)였음.
--      JPA 동작에는 큰 영향 없지만, 정합성을 원하면 아래를 적용.
DO $$
BEGIN
    -- pkey 이름이 기본이면 user_item_pkey일 가능성 높음. 없으면 스킵.
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'user_item_pkey'
          AND conrelid = 'user_item'::regclass
    ) THEN
ALTER TABLE user_item DROP CONSTRAINT user_item_pkey;
ALTER TABLE user_item ADD CONSTRAINT user_item_pkey PRIMARY KEY (user_id, item_id);
END IF;
END$$;

-- 2-4. 코멘트(선택)
COMMENT ON TABLE user_item IS '유저 - 아이템 매핑';
COMMENT ON COLUMN user_item.user_id IS '유저 ID';
COMMENT ON COLUMN user_item.item_id IS '아이템 ID';
COMMENT ON COLUMN user_item.purchased_at IS '구매 일시';
COMMENT ON COLUMN user_item.is_equipped IS '착용 여부';

-- 2-5. (선택) 조회 성능용 인덱스
CREATE INDEX IF NOT EXISTS idx_user_item_user_id ON user_item(user_id);
CREATE INDEX IF NOT EXISTS idx_user_item_item_id ON user_item(item_id);