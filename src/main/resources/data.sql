INSERT INTO users (
    name,
    email,
    password,
    status,
    is_platform_admin,
    created_at,
    updated_at
)
SELECT
    'Seed Platform Admin',
    'seed.admin@arqemio.com',
    '$2y$12$dafzDoAtVN4c15TOoypSkeYYbvBri/3TT9Y7DAXq04Lm9cLNgPTTC',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'seed.admin@arqemio.com'
);