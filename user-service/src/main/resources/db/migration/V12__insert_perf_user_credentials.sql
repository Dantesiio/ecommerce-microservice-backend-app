
INSERT INTO users
(first_name, last_name, email) VALUES
('perf', 'user', 'perf.user@example.com');

INSERT INTO credentials
(user_id, username, password, role, is_enabled) VALUES
((SELECT user_id FROM users WHERE email = 'perf.user@example.com'), 'perf.user', '$2b$04$ZhabZzr98f6fiWu4EpgtXuU3mgi4AcQ0FU7E5zDeuC3wbB0g5o2xK', 'ROLE_USER', true);
