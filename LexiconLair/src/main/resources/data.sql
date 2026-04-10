INSERT INTO dictionary_entry (ID, ENTRY_WORD, DEFINITION, DATE_ADDED, USERNAME, EXTERNAL_LOOKUP_COMPLETED)
SELECT 1000, 'ronan', 'A Student of the University of Life', CURRENT_DATE, 'ronan', false
FROM (SELECT 1) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM dictionary_entry WHERE ID = 1000
);
