-- Create database (run this first if the database doesn't exist)
CREATE DATABASE IF NOT EXISTS ai_suggestion_db;

-- Use the database
USE ai_suggestion_db;

-- Create the study_plan table
CREATE TABLE IF NOT EXISTS study_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_key VARCHAR(255) NOT NULL,
    user_key VARCHAR(255) NOT NULL,
    thread_id VARCHAR(255) NOT NULL,
    study_plan JSON,
    course_outline JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_thread_id (thread_id),
    INDEX idx_course_user (course_key, user_key)
);
