-- V9: insights 컬럼 구조 변경
-- insights 컬럼은 JPA에 의해 이미 생성됨
-- insight_night, insight_content, insight_self 컬럼은 수동으로 이미 삭제됨

-- Add comment to insights column
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'report' AND column_name = 'insights'
    ) THEN
        EXECUTE 'COMMENT ON COLUMN report.insights IS ''인사이트 목록 (JSONB)''';
    END IF;
END $$;
