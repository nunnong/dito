-- Add insights JSONB column to report table
ALTER TABLE report ADD COLUMN insights JSONB;

-- Add comment for insights column
COMMENT ON COLUMN report.insights IS '인사이트 목록 (JSONB)';

-- Optional: Migrate existing data from insight_* columns to insights array
-- This converts the three separate insight columns into a single JSONB array
UPDATE report
SET insights = jsonb_build_array(
    CASE WHEN insight_night IS NOT NULL THEN
        jsonb_build_object(
            'type', 'NEUTRAL',
            'description', insight_night
        )
    END,
    CASE WHEN insight_content IS NOT NULL THEN
        jsonb_build_object(
            'type', 'NEUTRAL',
            'description', insight_content
        )
    END,
    CASE WHEN insight_self IS NOT NULL THEN
        jsonb_build_object(
            'type', 'NEUTRAL',
            'description', insight_self
        )
    END
) - NULL -- Remove null elements from array
WHERE insight_night IS NOT NULL OR insight_content IS NOT NULL OR insight_self IS NOT NULL;

-- Optional: Drop old insight columns after data migration
-- Uncomment these lines if you want to remove the old columns
-- ALTER TABLE report DROP COLUMN insight_night;
-- ALTER TABLE report DROP COLUMN insight_content;
-- ALTER TABLE report DROP COLUMN insight_self;
