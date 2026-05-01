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
             'admin',
             '$2a$10$h92fQqtr92el9k8G.Sj8jOg08th9rNPbarXUWSqwO8xR97NSDx7XW',
             'admin@ronanos.com',
             'Admin',
             'Admin',
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
