-- Seed admin user for development.
-- Generate a BCrypt hash with: new BCryptPasswordEncoder().encode("yourpassword")
-- or via Spring Shell: spring encodepassword yourpassword
INSERT INTO users (username, password, email, first_name, last_name, created_at, created_by)
VALUES (
    'admin',
    '$2a$10$h92fQqtr92el9k8G.Sj8jOg08th9rNPbarXUWSqwO8xR97NSDx7XW',
    'admin@ronanos.com',
    'Admin',
    'Admin',
    NOW(),
    -1
) ON CONFLICT (username) DO NOTHING;
