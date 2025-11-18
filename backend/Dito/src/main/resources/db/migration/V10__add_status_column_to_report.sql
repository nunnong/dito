-- Add status column to report table
ALTER TABLE report ADD COLUMN IF NOT EXISTS status VARCHAR(20);
