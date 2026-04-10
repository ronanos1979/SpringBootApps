-- schema-postgresql.sql
-- This file serves as a placeholder and does nothing.
BEGIN;

-- Creates a dummy table only if it doesn't exist, preventing errors
CREATE TABLE IF NOT EXISTS dummy_placeholder (
    id SERIAL PRIMARY KEY,
    dummy_col TEXT
);

-- Drop it immediately so it doesn't persist
DROP TABLE IF EXISTS dummy_placeholder;

COMMIT;