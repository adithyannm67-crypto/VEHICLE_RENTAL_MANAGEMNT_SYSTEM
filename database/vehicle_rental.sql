-- ===================================================================
-- Vehicle Rental Management System - Database Schema
-- ===================================================================

-- CREATE DATABASE IF NOT EXISTS vehicle_rental_db;
USE vehicle_rental_db;

-- ---------------------------------------------------------
-- users: stores both CUSTOMER and ADMIN accounts
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    phone       VARCHAR(15)  NOT NULL,
    password    VARCHAR(255) NOT NULL,   -- stored as SHA-256 hash, never plain text
    address     VARCHAR(255),
    role        VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER'  -- CUSTOMER or ADMIN
);

-- ---------------------------------------------------------
-- vehicles: fleet of cars/bikes available for rent
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id          INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_name        VARCHAR(100) NOT NULL,
    vehicle_type        VARCHAR(20)  NOT NULL,   -- CAR or BIKE
    registration_number VARCHAR(20)  NOT NULL UNIQUE,
    fuel_type           VARCHAR(20)  NOT NULL,
    seats               INT          NOT NULL,
    price_per_day       DECIMAL(10,2) NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE' -- AVAILABLE, RENTED, MAINTENANCE, INACTIVE
);

-- ---------------------------------------------------------
-- rentals: one row per booking made by a customer
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS rentals (
    rental_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL,
    vehicle_id    INT NOT NULL,
    pickup_date   DATE NOT NULL,
    return_date   DATE NOT NULL,
    total_days    INT NOT NULL,
    total_amount  DECIMAL(10,2) NOT NULL,
    rental_status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED', -- CONFIRMED, COMPLETED, CANCELLED
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)
);

-- ---------------------------------------------------------
-- payments: one row per (simulated) payment made for a rental
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    payment_id     INT AUTO_INCREMENT PRIMARY KEY,
    rental_id      INT NOT NULL,
    payment_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount         DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,   -- UPI, CARD, CASH
    payment_status VARCHAR(20) NOT NULL,   -- SUCCESS, FAILED
    FOREIGN KEY (rental_id) REFERENCES rentals(rental_id)
);

-- ---------------------------------------------------------
-- maintenance: service history for vehicles
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS maintenance (
    maintenance_id   INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id       INT NOT NULL,
    description      VARCHAR(255),
    maintenance_date DATE NOT NULL,
    cost             DECIMAL(10,2),
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)
);

-- ===================================================================
-- Seed data
-- ===================================================================

-- NOTE: The default admin account (admin@vrs.com / Admin@123) is created
-- automatically by the Java application the first time it runs, so that
-- the password is stored as a correct SHA-256 hash. You do not need to
-- insert it manually here.

-- Sample vehicles
INSERT INTO vehicles (vehicle_name, vehicle_type, registration_number, fuel_type, seats, price_per_day, status) VALUES
('Maruti Suzuki Swift', 'CAR', 'KL01AB1234', 'Petrol', 5, 1500.00, 'AVAILABLE'),
('Hyundai Creta', 'CAR', 'KL01AB5678', 'Diesel', 5, 2500.00, 'AVAILABLE'),
('Toyota Innova', 'CAR', 'KL01AB9012', 'Diesel', 7, 3200.00, 'AVAILABLE'),
('Honda Activa', 'BIKE', 'KL01BK1111', 'Petrol', 2, 500.00, 'AVAILABLE'),
('Royal Enfield Classic', 'BIKE', 'KL01BK2222', 'Petrol', 2, 900.00, 'AVAILABLE')
ON DUPLICATE KEY UPDATE vehicle_name = vehicle_name;
