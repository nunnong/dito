-- Dito LangGraph Database Initialization Script
-- This script is automatically executed when the PostgreSQL container starts

-- Enable UUID extension for generating unique IDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create schema for LangGraph checkpoints and state
-- LangGraph will automatically create its required tables:
-- - checkpoints: stores graph execution state
-- - checkpoint_writes: stores individual state updates
-- - checkpoint_blobs: stores large binary data

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE dito_langgraph TO dito_user;

-- Create additional tables for Dito application (optional)
-- These tables can be used for storing intervention metadata, user data, etc.

CREATE TABLE IF NOT EXISTS interventions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    intervention_type VARCHAR(100),
    nudge_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    thread_id UUID,
    run_id UUID
);

CREATE TABLE IF NOT EXISTS evaluations (
    id SERIAL PRIMARY KEY,
    intervention_id INTEGER REFERENCES interventions(id),
    user_id INTEGER NOT NULL,
    effectiveness_score DECIMAL(3, 2),
    adjustment_needed BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    thread_id UUID,
    run_id UUID
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_interventions_user_id ON interventions(user_id);
CREATE INDEX IF NOT EXISTS idx_interventions_created_at ON interventions(created_at);
CREATE INDEX IF NOT EXISTS idx_evaluations_intervention_id ON evaluations(intervention_id);
CREATE INDEX IF NOT EXISTS idx_evaluations_user_id ON evaluations(user_id);

-- Grant permissions on tables
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO dito_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO dito_user;

-- Log initialization completion
DO $$
BEGIN
    RAISE NOTICE 'Dito LangGraph database initialized successfully';
END $$;
