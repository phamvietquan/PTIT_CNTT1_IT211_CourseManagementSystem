INSERT INTO users (username, password, email, full_name, role, active, created_at, updated_at)
SELECT 'admin', '$2a$10$D0KnIxSzDmn5u//a/7DO7uBDSms6L1dZVgAwkyWJBxEK0YxdspFvy', 'admin@coursemanagement.com', 'System Administrator', 'ADMIN', 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, email, full_name, role, active, created_at, updated_at)
SELECT 'lecturer01', '$2a$10$UZpr6JyQbp4.J2K36SnD5Ofic.y5bW5OzeayvjozIGFV9LlI.f94i', 'lecturer01@coursemanagement.com', 'Nguyễn Văn Giảng Viên', 'LECTURER', 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'lecturer01');
