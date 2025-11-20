-- Add report_date column to report table
ALTER TABLE report ADD COLUMN IF NOT EXISTS report_date DATE DEFAULT CURRENT_DATE;

COMMENT ON COLUMN report.report_date IS '리포트 대상 날짜';
