CREATE DATABASE IF NOT EXISTS corporate_visitors
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE corporate_visitors;

CREATE TABLE IF NOT EXISTS visits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    visitor_name VARCHAR(120) NOT NULL,
    company VARCHAR(160) NOT NULL,
    host_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    checked_in_at DATETIME NOT NULL,
    checked_out_at DATETIME NULL,
    allowed_minutes INT NOT NULL,
    reminder_sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_allowed_minutes CHECK (allowed_minutes > 0),
    INDEX idx_active_visits (checked_out_at, checked_in_at)
);

-- Optional application user. Change the password before using this in production.
-- CREATE USER 'visitor_app'@'localhost' IDENTIFIED BY 'change_this_password';
-- GRANT SELECT, INSERT, UPDATE ON corporate_visitors.* TO 'visitor_app'@'localhost';
-- FLUSH PRIVILEGES;
