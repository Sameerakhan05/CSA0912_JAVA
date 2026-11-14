CREATE DATABASE IF NOT EXISTS campus_events;
USE campus_events;

CREATE TABLE IF NOT EXISTS registrations (
    student_id VARCHAR(30) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    event_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (student_id, event_name)
);
