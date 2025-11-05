-- ============================================
-- Item 테이블 더미 데이터
-- ============================================

-- COSTUME 아이템들
INSERT INTO item (type, name, price, img_url, on_sale) VALUES
('COSTUME', '토마토 의상', 100, 'https://cdn.dito.app/items/costume_tomato.png', true),
('COSTUME', '바나나 의상', 150, 'https://cdn.dito.app/items/costume_banana.png', true),
('COSTUME', '당근 의상', 120, 'https://cdn.dito.app/items/costume_carrot.png', true),
('COSTUME', '수박 의상', 200, 'https://cdn.dito.app/items/costume_watermelon.png', true),
('COSTUME', '딸기 의상', 180, 'https://cdn.dito.app/items/costume_strawberry.png', false);

-- BACKGROUND 아이템들
INSERT INTO item (type, name, price, img_url, on_sale) VALUES
('BACKGROUND', '숲 배경', 80, 'https://cdn.dito.app/items/bg_forest.png', true),
('BACKGROUND', '바다 배경', 100, 'https://cdn.dito.app/items/bg_ocean.png', true),
('BACKGROUND', '우주 배경', 150, 'https://cdn.dito.app/items/bg_space.png', true),
('BACKGROUND', '도시 배경', 120, 'https://cdn.dito.app/items/bg_city.png', true),
('BACKGROUND', '사막 배경', 90, 'https://cdn.dito.app/items/bg_desert.png', false);

-- ============================================
-- User_Item 테이블 더미 데이터
-- 주의: user 테이블에 해당 user_id가 존재해야 합니다
-- ============================================

-- User 1이 구매한 아이템들
-- item_id는 위에서 생성된 순서대로 1~10번입니다
INSERT INTO user_item (user_id, item_id, purchased_at, is_equipped) VALUES
(1, 1, NOW(), true),   -- 토마토 의상 (장착)
(1, 6, NOW(), true),   -- 숲 배경 (장착)
(1, 2, NOW(), false),  -- 바나나 의상 (미장착)
(1, 7, NOW(), false);  -- 바다 배경 (미장착)

-- User 2가 구매한 아이템들
INSERT INTO user_item (user_id, item_id, purchased_at, is_equipped) VALUES
(2, 3, NOW(), true),   -- 당근 의상 (장착)
(2, 8, NOW(), true),   -- 우주 배경 (장착)
(2, 4, NOW(), false);  -- 수박 의상 (미장착)

-- User 3이 구매한 아이템들
INSERT INTO user_item (user_id, item_id, purchased_at, is_equipped) VALUES
(3, 4, NOW(), true),   -- 수박 의상 (장착)
(3, 9, NOW(), true),   -- 도시 배경 (장착)
(3, 1, NOW(), false),  -- 토마토 의상 (미장착)
(3, 6, NOW(), false);  -- 숲 배경 (미장착)

-- ============================================
-- 조회 쿼리 예시
-- ============================================

-- 모든 아이템 조회
-- SELECT * FROM item;

-- User 1의 장착된 아이템만 조회
-- SELECT i.*
-- FROM user_item ui
-- JOIN item i ON ui.item_id = i.id
-- WHERE ui.user_id = 1 AND ui.is_equipped = true;
