CREATE DATABASE IF NOT EXISTS iot_telemetry;
USE iot_telemetry;

CREATE TABLE IF NOT EXISTS telemetry_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sensor_id VARCHAR(50) NOT NULL,
    temperature FLOAT NOT NULL,
    smoke_density FLOAT NOT NULL,
    status VARCHAR(20) DEFAULT 'NORMAL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);