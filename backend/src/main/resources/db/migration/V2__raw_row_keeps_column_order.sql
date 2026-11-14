-- jsonb reorders object keys; json keeps the original CSV column order for display.
-- Re-run ingestion after this migration so existing rows get the original order back.
ALTER TABLE test_condition ALTER COLUMN raw_row TYPE json USING raw_row::json;
