-- Database initialization script for Job Recommendation System
-- Creates database schema, tables, indexes, and sample data
-- Author: Leo Ji

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS jobrec 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE jobrec;

-- Drop existing tables in correct order (foreign key dependencies)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS keywords;
DROP TABLE IF EXISTS history;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- Create users table
CREATE TABLE users (
    user_id VARCHAR(255) NOT NULL PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    
    INDEX idx_email (email),
    INDEX idx_created_at (created_at),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create items table (job listings)
CREATE TABLE items (
    item_id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    company_name VARCHAR(255),
    address VARCHAR(500),
    url VARCHAR(1000),
    description TEXT,
    salary_range VARCHAR(100),
    job_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_company (company_name),
    INDEX idx_job_type (job_type),
    INDEX idx_created_at (created_at),
    FULLTEXT idx_description (description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create keywords table (job keywords - many-to-many relationship)
CREATE TABLE keywords (
    item_id VARCHAR(255) NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    relevance_score DECIMAL(3,2) DEFAULT 1.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (item_id, keyword),
    FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE,
    
    INDEX idx_keyword (keyword),
    INDEX idx_relevance (relevance_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create history table (user favorite items)
CREATE TABLE history (
    user_id VARCHAR(255) NOT NULL,
    item_id VARCHAR(255) NOT NULL,
    last_favor_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    status ENUM('favorited', 'applied', 'interviewed', 'rejected', 'hired') DEFAULT 'favorited',
    
    PRIMARY KEY (user_id, item_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE,
    
    INDEX idx_user_favor_time (user_id, last_favor_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user sessions table for better session management
CREATE TABLE user_sessions (
    session_id VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create search history table for analytics
CREATE TABLE search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255),
    search_query VARCHAR(500),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    results_count INT DEFAULT 0,
    search_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    response_time_ms INT,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_user_search_time (user_id, search_time),
    INDEX idx_location (latitude, longitude),
    INDEX idx_search_time (search_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample users
-- Password is MD5 hash of "password123"
INSERT INTO users (user_id, password, first_name, last_name, email) VALUES
('admin', '482c811da5d5b4bc6d497ffa98491e38', 'Admin', 'User', 'admin@jobrec.com'),
('john_doe', '482c811da5d5b4bc6d497ffa98491e38', 'John', 'Doe', 'john.doe@example.com'),
('jane_smith', '482c811da5d5b4bc6d497ffa98491e38', 'Jane', 'Smith', 'jane.smith@example.com'),
('test_user', '482c811da5d5b4bc6d497ffa98491e38', 'Test', 'User', 'test@example.com');

-- Insert sample job items
INSERT INTO items (item_id, name, company_name, address, url, description, job_type) VALUES
('job_001', 'Senior Software Engineer', 'TechCorp Inc.', 'San Francisco, CA', 'https://example.com/job1', 'Develop and maintain web applications using modern technologies.', 'Full-time'),
('job_002', 'Data Scientist', 'DataFlow LLC', 'New York, NY', 'https://example.com/job2', 'Analyze large datasets and build machine learning models.', 'Full-time'),
('job_003', 'Frontend Developer', 'WebDesign Co.', 'Austin, TX', 'https://example.com/job3', 'Create responsive and interactive user interfaces.', 'Contract'),
('job_004', 'DevOps Engineer', 'CloudTech Solutions', 'Seattle, WA', 'https://example.com/job4', 'Manage cloud infrastructure and deployment pipelines.', 'Full-time'),
('job_005', 'Product Manager', 'Innovation Labs', 'Los Angeles, CA', 'https://example.com/job5', 'Lead product development and strategy initiatives.', 'Full-time');

-- Insert sample keywords
INSERT INTO keywords (item_id, keyword, relevance_score) VALUES
('job_001', 'Java', 0.95),
('job_001', 'Spring Boot', 0.90),
('job_001', 'REST API', 0.85),
('job_001', 'Microservices', 0.80),
('job_002', 'Python', 0.95),
('job_002', 'Machine Learning', 0.90),
('job_002', 'TensorFlow', 0.85),
('job_002', 'SQL', 0.80),
('job_003', 'JavaScript', 0.95),
('job_003', 'React', 0.90),
('job_003', 'CSS', 0.85),
('job_003', 'HTML', 0.80),
('job_004', 'Docker', 0.95),
('job_004', 'Kubernetes', 0.90),
('job_004', 'AWS', 0.85),
('job_004', 'CI/CD', 0.80),
('job_005', 'Product Strategy', 0.95),
('job_005', 'Agile', 0.90),
('job_005', 'User Research', 0.85),
('job_005', 'Analytics', 0.80);

-- Insert sample favorites
INSERT INTO history (user_id, item_id, status) VALUES
('john_doe', 'job_001', 'favorited'),
('john_doe', 'job_004', 'applied'),
('jane_smith', 'job_002', 'favorited'),
('jane_smith', 'job_003', 'interviewed'),
('test_user', 'job_005', 'favorited');

-- Create views for common queries
CREATE VIEW user_favorite_jobs AS
SELECT 
    u.user_id,
    u.first_name,
    u.last_name,
    i.item_id,
    i.name as job_title,
    i.company_name,
    i.address,
    h.last_favor_time,
    h.status
FROM users u
JOIN history h ON u.user_id = h.user_id
JOIN items i ON h.item_id = i.item_id
WHERE u.is_active = TRUE;

CREATE VIEW job_with_keywords AS
SELECT 
    i.item_id,
    i.name as job_title,
    i.company_name,
    i.address,
    i.job_type,
    GROUP_CONCAT(k.keyword ORDER BY k.relevance_score DESC SEPARATOR ', ') as keywords
FROM items i
LEFT JOIN keywords k ON i.item_id = k.item_id
GROUP BY i.item_id, i.name, i.company_name, i.address, i.job_type;

-- Create stored procedures for common operations
DELIMITER //

CREATE PROCEDURE GetUserFavorites(IN p_user_id VARCHAR(255))
BEGIN
    SELECT 
        i.item_id,
        i.name,
        i.company_name,
        i.address,
        i.url,
        h.last_favor_time,
        h.status,
        GROUP_CONCAT(k.keyword ORDER BY k.relevance_score DESC SEPARATOR ', ') as keywords
    FROM history h
    JOIN items i ON h.item_id = i.item_id
    LEFT JOIN keywords k ON i.item_id = k.item_id
    WHERE h.user_id = p_user_id
    GROUP BY i.item_id, i.name, i.company_name, i.address, i.url, h.last_favor_time, h.status
    ORDER BY h.last_favor_time DESC;
END //

CREATE PROCEDURE AddToFavorites(
    IN p_user_id VARCHAR(255),
    IN p_item_id VARCHAR(255),
    IN p_job_title VARCHAR(500),
    IN p_company_name VARCHAR(255),
    IN p_address VARCHAR(500),
    IN p_url VARCHAR(1000)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- Insert or update item
    INSERT INTO items (item_id, name, company_name, address, url)
    VALUES (p_item_id, p_job_title, p_company_name, p_address, p_url)
    ON DUPLICATE KEY UPDATE
        name = VALUES(name),
        company_name = VALUES(company_name),
        address = VALUES(address),
        url = VALUES(url),
        updated_at = CURRENT_TIMESTAMP;
    
    -- Add to favorites
    INSERT INTO history (user_id, item_id)
    VALUES (p_user_id, p_item_id)
    ON DUPLICATE KEY UPDATE
        last_favor_time = CURRENT_TIMESTAMP;
    
    COMMIT;
END //

DELIMITER ;

-- Create indexes for better performance
CREATE INDEX idx_users_email_active ON users(email, is_active);
CREATE INDEX idx_items_company_type ON items(company_name, job_type);
CREATE INDEX idx_history_user_time ON history(user_id, last_favor_time DESC);
CREATE INDEX idx_keywords_keyword_score ON keywords(keyword, relevance_score DESC);

-- Show database statistics
SELECT 
    'Database Setup Complete' as Status,
    (SELECT COUNT(*) FROM users) as Users,
    (SELECT COUNT(*) FROM items) as Jobs,
    (SELECT COUNT(*) FROM keywords) as Keywords,
    (SELECT COUNT(*) FROM history) as Favorites;

COMMIT;
