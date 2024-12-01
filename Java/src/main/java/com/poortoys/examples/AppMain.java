/**
 * AppMain serves as the entry point for the MySQL-based Ticketing System Simulation.
 * It initializes DAOs, schema modifiers, and booking services, and runs a simulation for a specified event.
 */

package com.poortoys.examples;

// Importing necessary libraries and classes for JPA and simulation
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import com.poortoys.examples.dao.*;
import com.poortoys.examples.simulation.*;

public class AppMain {
	
    public static void main(String[] args) {
        EntityManagerFactory emf = null; // Factory for creating EntityManager instances
        EntityManager em = null; // EntityManager for interacting with the database
        
        try {
            System.out.println("Starting MySQL Ticketing System Simulation...");
            
            // Step 1: Initialize the EntityManagerFactory
            emf = Persistence.createEntityManagerFactory("ticketingsystem"); // Persistence unit name defined in persistence.xml
            System.out.println("EntityManagerFactory created successfully");
            
            // Step 2: Create an EntityManager for database interactions
            em = emf.createEntityManager();
            System.out.println("EntityManager created successfully");
            
            // Step 3: Initialize DAOs for CRUD operations
            BookingDAO bookingDAO = new BookingDAO(em); // DAO for Booking entities
            TicketDAO ticketDAO = new TicketDAO(em); // DAO for Ticket entities
            UserDAO userDAO = new UserDAO(em); // DAO for User entities
            EventDAO eventDAO = new EventDAO(em); // DAO for Event entities
            BookingTicketDAO bookingTicketDAO = new BookingTicketDAO(em); // DAO for Booking-Ticket relationships
            System.out.println("DAOs initialized successfully");

            // Step 4: Initialize the schema modifier for altering database schemas
            MySQLSchemaModifier schemaModifier = new MySQLSchemaModifier(em);
            System.out.println("Schema modifier initialized successfully");

            // Step 5: Execute schema modifications
            try {
                System.out.println("\nExecuting schema modifications...");
                
                // Example: Add metadata fields to the booking table
                long duration = schemaModifier.modifySchema("add_booking_metadata");
                System.out.println("Schema modification completed in " + duration + " ms");

                // Print schema modification metrics
                Map<String, Object> metrics = schemaModifier.getMetrics(); // Retrieve metrics from the schema modifier
                System.out.println("\nSchema Modification Metrics:");
                metrics.forEach((key, value) -> 
                    System.out.printf("%-25s: %s%n", key, value)); // Format and display each metric
                
            } catch (Exception e) {
                System.err.println("Schema modification failed: " + e.getMessage());
            }

            // Step 6: Initialize the BookingService for handling booking logic
            BookingService bookingService = new BookingService(em);
            System.out.println("BookingService initialized successfully");

            // Step 7: Create a BookingSimulation instance
            BookingSimulation simulation = new BookingSimulation(
                bookingService, // Service to manage booking logic
                userDAO, // DAO for users
                eventDAO, // DAO for events
                ticketDAO // DAO for tickets
            );
            System.out.println("BookingSimulation created successfully");

            // Step 8: Run the simulation for a specified event ID
            System.out.println("\nStarting simulation for Event ID: 3");
            simulation.runSimulation(1); // Simulate bookings for Event ID 3
            
            // Prompt user to keep the console open after simulation
            System.out.println("\nSimulation completed. Press Enter to exit...");
            System.in.read();

        } catch (Exception e) {
            // Handle exceptions during execution
            System.err.println("Error during execution:");
            e.printStackTrace(); // Print the exception stack trace for debugging

            // Keep the console open if an error occurs
            try {
                System.out.println("Press Enter to exit...");
                System.in.read();
            } catch (Exception ex) {
                // Ignore any additional errors
            }
        } finally {
            // Step 9: Clean up resources to avoid memory leaks
            if (em != null && em.isOpen()) {
                try {
                    // Rollback active transactions if any
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    em.close(); // Close the EntityManager
                    System.out.println("EntityManager closed successfully");
                } catch (Exception e) {
                    System.err.println("Error closing EntityManager: " + e.getMessage());
                }
            }
            if (emf != null && emf.isOpen()) {
                try {
                    emf.close(); // Close the EntityManagerFactory
                    System.out.println("EntityManagerFactory closed successfully");
                } catch (Exception e) {
                    System.err.println("Error closing EntityManagerFactory: " + e.getMessage());
                }
            }
        }
    }
}
