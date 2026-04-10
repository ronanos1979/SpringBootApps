CREATE PROCEDURE RenameColumnIfexists()
BEGIN
  -- Check if the column exists in the specific table and schema
  IF EXISTS (
    SELECT 1 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'dictionaryEntry' 
      AND COLUMN_NAME = 'target_date'
  ) THEN
    -- Execute rename if column is found
    ALTER TABLE dictionaryEntry RENAME COLUMN target_date TO date_added;
  END IF;
END $$
