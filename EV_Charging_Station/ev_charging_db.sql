-- ============================================================
-- EV Charging Station Slot Booking & Billing System | MySQL schema
-- Database : ev_charging_db
-- SIMATS Engineering · CSA0926 Java Programming
-- ============================================================
CREATE DATABASE IF NOT EXISTS ev_charging_db;
USE ev_charging_db;

-- ---------- Users / Customers ----------
CREATE TABLE IF NOT EXISTS users (
    user_id   VARCHAR(15) PRIMARY KEY,
    name      VARCHAR(60) NOT NULL,
    email     VARCHAR(60) NOT NULL,
    password  VARCHAR(60) NOT NULL
);

-- ---------- Vehicles ----------
CREATE TABLE IF NOT EXISTS vehicle (
    vehicle_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id         VARCHAR(15) NOT NULL,
    vehicle_number  VARCHAR(20) NOT NULL,
    vehicle_type    VARCHAR(30) NOT NULL,
    CONSTRAINT fk_vehicle_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ---------- Charging Stations ----------
CREATE TABLE IF NOT EXISTS charging_station (
    station_id    INT PRIMARY KEY,
    station_name  VARCHAR(60)  NOT NULL,
    location      VARCHAR(100) NOT NULL,
    charger_type  VARCHAR(30)  NOT NULL,
    rate_per_kwh  DECIMAL(8,2) NOT NULL
);

-- ---------- Charging Slots ----------
CREATE TABLE IF NOT EXISTS charging_slot (
    slot_id    INT AUTO_INCREMENT PRIMARY KEY,
    station_id INT NOT NULL,
    slot_time  VARCHAR(30) NOT NULL,
    status     VARCHAR(20) NOT NULL,
    CONSTRAINT fk_slot_station
    FOREIGN KEY (station_id) REFERENCES charging_station(station_id)
);

-- ---------- Bookings ----------
CREATE TABLE IF NOT EXISTS bookings (
    booking_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id      VARCHAR(15) NOT NULL,
    vehicle_id   INT NOT NULL,
    station_id   INT NOT NULL,
    slot_id      INT NOT NULL,
    booking_date DATE NOT NULL,
    status       VARCHAR(20) NOT NULL,
    CONSTRAINT fk_booking_user    FOREIGN KEY (user_id)    REFERENCES users(user_id),
    CONSTRAINT fk_booking_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id),
    CONSTRAINT fk_booking_station FOREIGN KEY (station_id) REFERENCES charging_station(station_id),
    CONSTRAINT fk_booking_slot    FOREIGN KEY (slot_id)    REFERENCES charging_slot(slot_id)
);

-- ---------- Bills ----------
CREATE TABLE IF NOT EXISTS bills (
    bill_id         INT AUTO_INCREMENT PRIMARY KEY,
    booking_id      INT NOT NULL,
    units_consumed  DECIMAL(8,2)  NOT NULL,
    rate_per_kwh    DECIMAL(8,2)  NOT NULL,
    charging_cost   DECIMAL(10,2) NOT NULL,
    booking_fee     DECIMAL(10,2) NOT NULL,
    tax             DECIMAL(10,2) NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    payment_status  VARCHAR(20)   NOT NULL,
    CONSTRAINT fk_bill_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

-- ---------- Sample data ----------
INSERT INTO users(user_id,name,email,password) VALUES
('U101','Customer One','customer1@gmail.com','pw'),
('U102','Customer Two','customer2@gmail.com','pw'),
('U103','Customer Three','customer3@gmail.com','pw');

INSERT INTO vehicle(user_id,vehicle_number,vehicle_type) VALUES
('U101','AP01AB1234','Electric Car'),
('U102','AP02CD5678','Electric Car'),
('U103','AP03EF9012','Electric Bike');

INSERT INTO charging_station(station_id,station_name,location,charger_type,rate_per_kwh) VALUES
(1,'Green Charge Station','Kadapa','Fast Charger',12.00),
(2,'EV Power Station','Chennai','DC Charger',15.00),
(3,'Smart EV Station','Bangalore','AC Charger',10.00);

INSERT INTO charging_slot(station_id,slot_time,status) VALUES
(1,'09:00 AM - 10:00 AM','AVAILABLE'),
(1,'10:00 AM - 11:00 AM','AVAILABLE'),
(1,'11:00 AM - 12:00 PM','AVAILABLE'),
(2,'09:00 AM - 10:00 AM','AVAILABLE'),
(2,'10:00 AM - 11:00 AM','AVAILABLE'),
(3,'09:00 AM - 10:00 AM','AVAILABLE');

-- ---------- Sample queries ----------
-- SELECT all charging stations:
SELECT * FROM charging_station;

-- SELECT available charging slots:
SELECT * FROM charging_slot WHERE status = 'AVAILABLE';

-- UPDATE charging rate:
UPDATE charging_station SET rate_per_kwh = 13.00 WHERE station_id = 1;

-- UPDATE slot status after booking:
UPDATE charging_slot SET status = 'BOOKED' WHERE slot_id = 1;

-- DELETE a charging station:
DELETE FROM charging_station WHERE station_id = 3;
