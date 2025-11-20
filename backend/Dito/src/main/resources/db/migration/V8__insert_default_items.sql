-- ==============================================
-- V8__insert_default_items.sql
-- ==============================================
-- 목적: item 테이블에 기본 아이템 데이터 삽입
-- 작성일: 2025-11-10
-- 변경내용:
--   - 기본 의상(COSTUME) 및 배경(BACKGROUND) 아이템 등록
--   - 초기 사용자 스토어 구성을 위한 프리셋 데이터 추가
-- ==============================================

-- 1. Insert default costume items
INSERT INTO item (type, name, price, img_url, on_sale) VALUES
                                                           ('COSTUME', '레몬 의상', 0, 'https://k13a708.p.ssafy.io/media/lemon.png', false),
                                                           ('COSTUME', '포도 의상', 120, 'https://k13a708.p.ssafy.io/media/grape.png', true),
                                                           ('COSTUME', '키위 의상', 100, 'https://k13a708.p.ssafy.io/media/kiwi.png', true),
                                                           ('COSTUME', '메론 의상', 100, 'https://k13a708.p.ssafy.io/media/melon.png', true),
                                                           ('COSTUME', '오렌지 의상', 100, 'https://k13a708.p.ssafy.io/media/orange.png', true),
                                                           ('COSTUME', '토마토 의상', 100, 'https://k13a708.p.ssafy.io/media/tomato.png', true);

-- 2. Insert default background items
INSERT INTO item (type, name, price, img_url, on_sale) VALUES
                                                           ('BACKGROUND', '기본 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/default.png', true),
                                                           ('BACKGROUND', '야구 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/baseball.png', true),
                                                           ('BACKGROUND', '해변 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/beach.png', true),
                                                           ('BACKGROUND', '과일가게 배경1', 100, 'https://k13a708.p.ssafy.io/media/bg/fruits-store.png', true),
                                                           ('BACKGROUND', '제주도 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/jeju.png', true),
                                                           ('BACKGROUND', '우주 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/space.png', true),
                                                           ('BACKGROUND', '새 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/bird.png', true),
                                                           ('BACKGROUND', '부산 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/busan.png', true),
                                                           ('BACKGROUND', '크리스마스 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/christmas.png', true),
                                                           ('BACKGROUND', '화이트 크리스마스 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/christmas-snow.png', true),
                                                           ('BACKGROUND', '콘서트 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/concert.png', true),
                                                           ('BACKGROUND', '사막 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/desert.png', true),
                                                           ('BACKGROUND', '과일 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/fruit.png', true),
                                                           ('BACKGROUND', '과일가게 배경2', 100, 'https://k13a708.p.ssafy.io/media/bg/fruits-store2.png', true),
                                                           ('BACKGROUND', '한강 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/hanriver.png', true),
                                                           ('BACKGROUND', '집 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/home.png', true),
                                                           ('BACKGROUND', '달 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/moon.png', true),
                                                           ('BACKGROUND', '밤 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/night.png', true),
                                                           ('BACKGROUND', '해변 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/ocean.png', true),
                                                           ('BACKGROUND', '소풍 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/picnic.png', true),
                                                           ('BACKGROUND', '분홍 하늘 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/pinksky.png', true),
                                                           ('BACKGROUND', '놀이터 배경1', 100, 'https://k13a708.p.ssafy.io/media/bg/playground.png', true),
                                                           ('BACKGROUND', '놀이터 배경2', 100, 'https://k13a708.p.ssafy.io/media/bg/hanriver.png2', true),
                                                           ('BACKGROUND', '트랙 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/road.png', true),
                                                           ('BACKGROUND', '학교 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/school.png', true),
                                                           ('BACKGROUND', '양때 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/sheep.png', true),
                                                           ('BACKGROUND', '쇼핑몰 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/shopping.png', true),
                                                           ('BACKGROUND', '토마토 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/tomato.png', true),
                                                           ('BACKGROUND', '나무 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/tree.png', true),
                                                           ('BACKGROUND', '은하계 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/universe.png', true),
                                                           ('BACKGROUND', '화산 배경', 100, 'https://k13a708.p.ssafy.io/media/bg/volcano.png', true);

-- ==============================================
-- Migration Notes:
-- - 초기 배경 및 의상 데이터가 자동으로 삽입됩니다.
-- - 기존 item 데이터가 중복되지 않도록 Flyway가 신규 버전(V8)으로 관리합니다.
-- - 이후 아이템 추가는 V9 이상 버전에서 별도 관리합니다.
-- ==============================================
