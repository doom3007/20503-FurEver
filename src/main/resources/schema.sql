-- FurEver Database Schema
-- Pet Adoption System Database

CREATE DATABASE IF NOT EXISTS furever CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE furever;
SET NAMES utf8mb4;

-- Category table
CREATE TABLE IF NOT EXISTS Category (
    categoryID INT AUTO_INCREMENT PRIMARY KEY,
    categoryName VARCHAR(50) NOT NULL UNIQUE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Pet table
CREATE TABLE IF NOT EXISTS Pet (
    petID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    categoryID INT NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    description TEXT,
    imagePath VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'זמינה',
    publishDate DATE NOT NULL,
    ownerName VARCHAR(100) NOT NULL,
    ownerPhone VARCHAR(20) NOT NULL,
    ownerEmail VARCHAR(100) NOT NULL,
    FOREIGN KEY (categoryID) REFERENCES Category(categoryID)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AdoptionRequest table
CREATE TABLE IF NOT EXISTS AdoptionRequest (
    requestID INT AUTO_INCREMENT PRIMARY KEY,
    petID INT NOT NULL,
    message TEXT,
    requestDate DATE NOT NULL,
    requestStatus VARCHAR(20) NOT NULL DEFAULT 'נשלחה',
    requesterName VARCHAR(100) NOT NULL,
    requesterPhone VARCHAR(20) NOT NULL,
    requesterEmail VARCHAR(100) NOT NULL,
    FOREIGN KEY (petID) REFERENCES Pet(petID)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Users table for authentication
CREATE TABLE IF NOT EXISTS User (
    userID INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fullName VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    isAdmin BOOLEAN NOT NULL DEFAULT FALSE,
    registrationDate DATE NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Insert sample categories
SET NAMES utf8mb4;
INSERT INTO Category (categoryName) VALUES 
('כלב'),
('חתול'),
('ארנב'),
('תוכי'),
('אחר');

-- Insert default admin user (password: admin123)
-- Password is now hashed using BCrypt
INSERT INTO User (username, password, fullName, email, phone, isAdmin, registrationDate) VALUES 
('admin', '$2a$12$Rl71kLBLqYGDZEl1Nq4T4uk85/5xGkiDGKcL7lE21gjf/MkcFoKCq', 'מנהל מערכת', 'admin@furever.com', '+972501234567', TRUE, CURDATE());

-- Insert test users (password: 123456)
-- Passwords are now hashed using BCrypt
INSERT INTO User (username, password, fullName, email, phone, isAdmin, registrationDate) VALUES 
('test1', '$2a$12$gWxSqmmrEkZWo/60SL51UO5bE9btZ/XupIK.s7yUUatsyCV3r82RC', 'a simple test1 user', 'test1@user.com', '0500000001', FALSE, CURDATE());

INSERT INTO User (username, password, fullName, email, phone, isAdmin, registrationDate) VALUES 
('test2', '$2a$12$gWxSqmmrEkZWo/60SL51UO5bE9btZ/XupIK.s7yUUatsyCV3r82RC', 'a simple test2 user', 'test2@user.com', '0500000002', FALSE, CURDATE());

-- Insert sample pet for testing (owned by test1)
INSERT INTO Pet (name, categoryID, age, gender, description, imagePath, status, publishDate, ownerName, ownerPhone, ownerEmail) VALUES
("ג'ינג'ר", 2, 9, 'נקבה', "חתולה ג'ינג'ית היפראקטיבית", null, 'זמינה', CURDATE(), 'a simple test1 user', '0500000001', 'test1@user.com');

-- Insert sample adoption request from test2 for the pet
-- Use a subquery to get the specific pet we just inserted to ensure correct petID
INSERT INTO AdoptionRequest (petID, message, requestDate, requestStatus, requesterName, requesterPhone, requesterEmail)
SELECT petID, 'זה כלב?', CURDATE(), 'נשלחה', 'a simple test2 user', '0500000002', 'test2@user.com'
FROM Pet WHERE name = "ג'ינג'ר" AND ownerEmail = 'test1@user.com' LIMIT 1;
