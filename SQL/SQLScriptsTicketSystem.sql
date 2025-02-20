-- MySQL Implementation

CREATE DATABASE TicketSystem;
USE TicketSystem;

-- Create Tables
-- Genre Table categorizes Performers by their respective genres
CREATE TABLE genres (
    genre_id INT PRIMARY KEY AUTO_INCREMENT, -- Unique identifier for each genre
    genre_name VARCHAR(35) NOT NULL          -- Name of the genre, limited to 35 characters
);

-- Performer Table represents individuals or groups performing at events
CREATE TABLE performers (
    performer_id INT PRIMARY KEY AUTO_INCREMENT, -- Unique identifier for each performer
    performer_name VARCHAR(100) NOT NULL,        -- Name of the performer, up to 100 characters
    genre_id INT,                                 -- Foreign key linking to the genre table
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id) -- Ensures referential integrity with genre
);

-- Venue Table stores information about event locations
CREATE TABLE venues (
    venue_id INT PRIMARY KEY AUTO_INCREMENT, -- Unique identifier for each venue
    venue_name VARCHAR(50) NOT NULL,         -- Name of the venue, up to 50 characters
    location VARCHAR(100) NOT NULL,          -- Address or location details of the venue
    type VARCHAR(30),                         -- Type of venue (e.g., Concert Hall, Stadium)
    capacity INT NOT NULL                     -- Maximum number of attendees the venue can hold
    -- Additional columns for seat mapping can be added here if needed
);

-- Event Table encapsulates details about each event
CREATE TABLE events (
    event_id INT PRIMARY KEY AUTO_INCREMENT, -- Unique identifier for each event
    event_name VARCHAR(100) NOT NULL,        -- Name of the event, up to 100 characters
    performer_id INT,                         -- Foreign key linking to the performer table
    event_date DATE NOT NULL,                 -- Date of the event
    venue_id INT,                             -- Foreign key linking to the venue table
    FOREIGN KEY (performer_id) REFERENCES performers(performer_id), -- Ensures referential integrity with performer
    FOREIGN KEY (venue_id) REFERENCES venues(venue_id)              -- Ensures referential integrity with venue
);

-- Ticket_Category Table defines different categories of tickets for events
CREATE TABLE ticket_category (
    ticket_category_id INT PRIMARY KEY AUTO_INCREMENT, -- Unique identifier for each ticket category
    description VARCHAR(100) NOT NULL,                  -- Description of the ticket category (e.g., VIP, General Admission)
    price DECIMAL(10,2) NOT NULL,                       -- Price of the ticket, allowing up to 99999999.99
    start_date TIMESTAMP NULL,                           -- Optional start date for the ticket category availability
    end_date TIMESTAMP NULL,                             -- Optional end date for the ticket category availability
    area VARCHAR(30) NULL,                               -- Specific area or section within the venue (e.g., Front Row)
    event_id INT,                                        -- Foreign key linking to the event table
    FOREIGN KEY (event_id) REFERENCES events(event_id)    -- Ensures referential integrity with event
);

-- Ticket Table holds information about individual tickets
CREATE TABLE tickets (
    ticket_id INT PRIMARY KEY AUTO_INCREMENT,         -- Unique identifier for each ticket
    serial_number VARCHAR(255) UNIQUE NOT NULL,       -- Unique serial number to prevent duplication
    event_id INT,                                      -- Foreign key linking to the event table
    ticket_category_id INT,                            -- Foreign key linking to the ticket_category table
    section VARCHAR(30) NOT NULL,                      -- Section of the venue (e.g., A, B, C)
    rownumber VARCHAR(10) NOT NULL,                   -- Row number within the section
    seat_number VARCHAR(10) NOT NULL,                  -- Seat number within the row
    status ENUM('available', 'sold') DEFAULT 'available', -- Status of the ticket to track availability
    purchase_date TIMESTAMP NULL,                       -- Timestamp of when the ticket was purchased
    FOREIGN KEY (event_id) REFERENCES events(event_id),  -- Ensures referential integrity with event
    FOREIGN KEY (ticket_category_id) REFERENCES ticket_category(ticket_category_id) -- Ensures referential integrity with ticket_category
);

-- User Table represents individuals interacting with the system
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,          -- Unique identifier for each user
    user_name VARCHAR(50) UNIQUE NOT NULL,           -- Unique username for login, up to 50 characters
    email VARCHAR(100) UNIQUE NOT NULL,              -- Unique email address for the user
    password_hash VARCHAR(255) NOT NULL,             -- Hashed password for secure authentication
    confirmation_code VARCHAR(100),                   -- Code for account verification
    confirmation_time TIMESTAMP                       -- Timestamp of when the account was confirmed
);

-- Booking Table records the details of ticket purchases by users
CREATE TABLE bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,        -- Unique identifier for each booking
    user_id INT,                                       -- Foreign key linking to the user table
    delivery_address_email VARCHAR(100),              -- Email address for ticket delivery
    delivery_time TIMESTAMP,                           -- Timestamp when tickets are delivered
    time_paid TIMESTAMP,                               -- Timestamp when payment was made
    time_sent TIMESTAMP,                               -- Timestamp when tickets were sent
    total_price DECIMAL(10,2) NOT NULL,                -- Total price before discounts
    discount DECIMAL(10,2) DEFAULT 0,                  -- Discount applied to the booking
    final_price DECIMAL(10,2) NOT NULL,                -- Final price after discounts
    booking_status ENUM('in-progress', 'confirmed', 'canceled') DEFAULT 'in-progress', -- Status of the booking
    FOREIGN KEY (user_id) REFERENCES users(user_id)     -- Ensures referential integrity with user
);

-- Booking_Ticket Table associates tickets with bookings
CREATE TABLE booking_ticket (
    booking_ticket_id INT PRIMARY KEY AUTO_INCREMENT, -- Unique identifier for each booking-ticket association
    booking_id INT,                                   -- Foreign key linking to the booking table
    ticket_id INT,                                    -- Foreign key linking to the ticket table
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id), -- Ensures referential integrity with booking
    FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id)      -- Ensures referential integrity with ticket
);