-- Start transaction to ensure data integrity
START TRANSACTION;

-- ======================
-- Insert into Genres Table
-- ======================
INSERT INTO genres (genre_name) VALUES
('Rock'),
('Jazz'),
('Classical'),
('Pop'),
('Electronic'),
('Hip-Hop'),
('Country'),
('Blues'),
('Reggae'),
('Metal');

-- ======================
-- Insert into Performers Table
-- ======================
INSERT INTO performers (performer_name, genre_id) VALUES
('The Rockers', 1),
('Jazz Masters', 2),
('Classical Quartet', 3),
('Pop Icons', 4),
('Electronica', 5),
('Hip-Hop Crew', 6),
('Country Stars', 7),
('Blues Band', 8),
('Reggae Rhythms', 9),
('Metal Heads', 10);

-- ======================
-- Insert into Venues Table
-- ======================
INSERT INTO venues (venue_name, location, type, capacity) VALUES
('Grand Arena', '123 Main St, Anytown', 'Stadium', 50000),
('Jazz Club', '456 Oak Ave, Music City', 'Club', 200),
('Symphony Hall', '789 Pine Rd, Harmony', 'Concert Hall', 1500),
('Pop Dome', '321 Maple St, Popville', 'Arena', 30000),
('Electronica Center', '654 Elm St, Tech City', 'Exhibition Center', 10000),
('Hip-Hop Venue', '987 Cedar Blvd, Beat Town', 'Club', 800),
('Country Grounds', '543 Birch Ln, Nashville', 'Open Air', 25000),
('Blues House', '210 Willow Dr, Bluesville', 'Bar', 150),
('Reggae Beach', '369 Ocean Ave, Island City', 'Beach', 5000),
('Metal Hall', '852 Steel Rd, Metal City', 'Hall', 7000);

-- ======================
-- Insert into Events Table
-- ======================
INSERT INTO events (event_name, performer_id, event_date, venue_id) VALUES
('Rock Fest 2024', 1, '2024-06-15', 1),
('Jazz Nights', 2, '2024-07-20', 2),
('Classical Evening', 3, '2024-08-05', 3),
('Pop Extravaganza', 4, '2024-09-10', 4),
('Electronic Beats', 5, '2024-10-25', 5),
('Hip-Hop Bash', 6, '2024-11-15', 6),
('Country Fair', 7, '2024-12-05', 7),
('Blues Festival', 8, '2025-01-20', 8),
('Reggae Summer', 9, '2025-02-25', 9),
('Metal Mania', 10, '2025-03-30', 10);

-- ======================
-- Insert into Ticket_Category Table
-- ======================
INSERT INTO ticket_category (description, price, start_date, end_date, area, event_id) VALUES
-- Rock Fest 2024 Categories
('VIP', 150.00, '2024-05-01 00:00:00', '2024-06-14 23:59:59', 'Front Row', 1),
('General Admission', 75.00, '2024-05-01 00:00:00', '2024-06-14 23:59:59', 'Middle Section', 1),
('Balcony', 50.00, '2024-05-01 00:00:00', '2024-06-14 23:59:59', 'Upper Balcony', 1),
-- Jazz Nights Categories
('VIP', 120.00, '2024-06-01 00:00:00', '2024-07-19 23:59:59', 'Front Stage', 2),
('General Admission', 60.00, '2024-06-01 00:00:00', '2024-07-19 23:59:59', 'Main Floor', 2),
-- Continue for other events...
('VIP', 130.00, '2024-07-01 00:00:00', '2024-08-04 23:59:59', 'Orchestra', 3),
('Standard', 70.00, '2024-07-01 00:00:00', '2024-08-04 23:59:59', 'Mezzanine', 3),
('VIP', 140.00, '2024-08-01 00:00:00', '2024-09-09 23:59:59', 'Golden Circle', 4),
('Standard', 80.00, '2024-08-01 00:00:00', '2024-09-09 23:59:59', 'General Area', 4),
-- ...Add categories for other events as needed

-- ======================
-- Insert into Tickets Table
-- ======================

-- Generate tickets for Rock Fest 2024
INSERT INTO tickets (serial_number, event_id, ticket_category_id, section, rownumber, seat_number, status, purchase_date) VALUES
('RF1001', 1, 1, 'A', '1', '1', 'available', NULL),
('RF1002', 1, 1, 'A', '1', '2', 'available', NULL),
('RF1003', 1, 1, 'A', '1', '3', 'available', NULL),
-- Continue generating tickets for VIP section
('RF1010', 1, 2, 'B', '5', '10', 'available', NULL),
('RF1011', 1, 2, 'B', '5', '11', 'available', NULL),
-- Continue generating tickets for General Admission
('RF1020', 1, 3, 'C', '10', '20', 'available', NULL),
('RF1021', 1, 3, 'C', '10', '21', 'available', NULL),
-- Continue generating tickets for Balcony
-- Repeat similar ticket generation for other events and categories

-- Example for Jazz Nights
('JN2001', 2, 4, 'Front', 'A', '1', 'available', NULL),
('JN2002', 2, 4, 'Front', 'A', '2', 'available', NULL),
('JN2003', 2, 5, 'Main', 'B', '5', 'available', NULL),
-- And so on for other events

-- Note: Extend the dataset to include sufficient tickets per event and category

-- ======================
-- Insert into Users Table
-- ======================
INSERT INTO users (user_name, email, password_hash, confirmation_code, confirmation_time) VALUES
('john_doe', 'john.doe@example.com', 'hashedpassword1', 'CONF123', '2024-01-15 10:00:00'),
('jane_smith', 'jane.smith@example.com', 'hashedpassword2', 'CONF124', '2024-02-20 11:30:00'),
('alice_jones', 'alice.jones@example.com', 'hashedpassword3', 'CONF125', '2024-03-25 09:45:00'),
('bob_brown', 'bob.brown@example.com', 'hashedpassword4', 'CONF126', '2024-04-10 14:20:00'),
('charlie_davis', 'charlie.davis@example.com', 'hashedpassword5', 'CONF127', '2024-05-05 16:50:00'),
-- Add more users to simulate high concurrency
('david_wilson', 'david.wilson@example.com', 'hashedpassword6', 'CONF128', '2024-06-10 12:15:00'),
('emma_thomas', 'emma.thomas@example.com', 'hashedpassword7', 'CONF129', '2024-07-22 13:40:00'),
('olivia_martin', 'olivia.martin@example.com', 'hashedpassword8', 'CONF130', '2024-08-18 15:55:00'),
('liam_jackson', 'liam.jackson@example.com', 'hashedpassword9', 'CONF131', '2024-09-12 17:05:00'),
('sophia_white', 'sophia.white@example.com', 'hashedpassword10', 'CONF132', '2024-10-05 18:25:00');

-- ======================
-- Insert into Bookings Table
-- ======================
INSERT INTO bookings (user_id, delivery_address_email, delivery_time, time_paid, time_sent, total_price, discount, final_price, booking_status) VALUES
(1, 'john.doe@example.com', '2024-06-15 12:00:00', '2024-06-15 11:50:00', '2024-06-15 12:30:00', 300.00, 50.00, 250.00, 'confirmed'),
(2, 'jane.smith@example.com', '2024-07-20 15:00:00', '2024-07-20 14:45:00', '2024-07-20 15:30:00', 180.00, 0.00, 180.00, 'in-progress'),
(3, 'alice.jones@example.com', '2024-08-05 18:00:00', '2024-08-05 17:55:00', '2024-08-05 18:30:00', 75.00, 5.00, 70.00, 'confirmed'),
(4, 'bob.brown@example.com', '2024-09-10 20:00:00', '2024-09-10 19:50:00', '2024-09-10 20:30:00', 225.00, 25.00, 200.00, 'canceled'),
(5, 'charlie.davis@example.com', '2024-10-25 22:00:00', '2024-10-25 21:45:00', '2024-10-25 22:30:00', 150.00, 10.00, 140.00, 'confirmed'),
(6, 'david.wilson@example.com', '2024-11-15 20:00:00', '2024-11-15 19:50:00', '2024-11-15 20:30:00', 90.00, 0.00, 90.00, 'confirmed'),
(7, 'emma.thomas@example.com', '2024-12-05 21:00:00', '2024-12-05 20:45:00', '2024-12-05 21:30:00', 120.00, 20.00, 100.00, 'in-progress'),
(8, 'olivia.martin@example.com', '2025-01-20 18:00:00', '2025-01-20 17:50:00', '2025-01-20 18:30:00', 60.00, 0.00, 60.00, 'confirmed'),
(9, 'liam.jackson@example.com', '2025-02-25 19:00:00', '2025-02-25 18:45:00', '2025-02-25 19:30:00', 200.00, 30.00, 170.00, 'confirmed'),
(10, 'sophia.white@example.com', '2025-03-30 20:00:00', '2025-03-30 19:50:00', '2025-03-30 20:30:00', 250.00, 50.00, 200.00, 'canceled');

-- ======================
-- Insert into Booking_Ticket Table
-- ======================
INSERT INTO booking_ticket (booking_id, ticket_id) VALUES
(1, 1),
(1, 2),
(1, 3),
(2, 4),
(2, 10),
(3, 6),
(4, 7),
(4, 8),
(5, 9),
(5, 5),
(6, 11),
(7, 12),
(7, 13),
(8, 14),
(9, 15),
(9, 16),
(9, 17),
(10, 18),
(10, 19);

-- Commit the transaction
COMMIT;
