
# Database Performance Analysis in High-Concurrency Ticketing Systems

This research project implements and compares MySQL and MongoDB in a high-concurrency ticketing system environment, providing empirical evidence to support database selection decisions in similar applications.

## Research Overview

The study evaluates how MySQL and MongoDB handle increasing concurrent loads (from 1 to 5,000 users) while managing ticket bookings, focusing on:
- Transactional behavior under high concurrency
- Impact of schema design on performance
- Management of nested data structures

## Technical Implementation

The project uses Java to implement identical ticketing system functionality across both databases:

### Core Technologies
- MySQL Community Server 8.0
- MongoDB Community Server 8.0.3
- OpenJDK 23.0.1
- Maven 3.9.5
- Hibernate (MySQL ORM)
- Morphia (MongoDB ODM)

### System Architecture
The implementation follows a layered design:
- Data Access Layer: Database-specific implementations
- Service Layer: Business logic and transaction management
- Simulation Framework: Concurrent booking operations

## Key Findings

The research revealed distinct performance characteristics:
- MySQL demonstrated superior consistency in transaction processing but with longer total processing times
- MongoDB showed better scalability under high load but with more variable performance patterns
- Schema modification impacts varied significantly between the two systems

## Running the Tests

Detailed instructions for setting up and running the test scenarios are available in the docs directory.

## Research Documentation

Full research findings, including detailed performance metrics and analysis, are available in the thesis document under the docs directory.

## Author
Gabriel Alberto Avina Solares

