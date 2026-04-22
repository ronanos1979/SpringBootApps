INSERT INTO users (
    username,
    password,
    email,
    first_name,
    last_name,
    created_at,
    updated_at,
    created_by,
    updated_by
) VALUES (
             'ronan',
             '$2a$10$REPLACE_WITH_REAL_BCRYPT_HASH',
             'ronan@example.com',
             'Ronan',
             'OSullivan',
             NOW(),
             NULL,
             -1,
             NULL
         )
    ON CONFLICT (username) DO NOTHING;


INSERT INTO word (TEXT, LANGUAGE)
SELECT 'ronan', 'en'
FROM (SELECT 1) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM word WHERE TEXT = 'ronan' and LANGUAGE = 'en'
);
