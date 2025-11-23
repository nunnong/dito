-- Add strategy column to report table
ALTER TABLE report ADD COLUMN IF NOT EXISTS strategy JSONB NOT NULL DEFAULT '[]'::jsonb;

-- Add comment to strategy column
COMMENT ON COLUMN report.strategy IS '전략 변경 이력 목록 (JSONB)';
