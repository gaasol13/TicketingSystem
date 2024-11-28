package com.poortoys.examples;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.poortoys.examples.dao.*;
import com.poortoys.examples.simulation.*;

public class AppMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        
        try {
            System.out.println("Starting MySQL Ticketing System Simulation...");
            // Create EntityManagerFactory
            emf = Persistence.createEntityManagerFactory("ticketingsystem");
            System.out.println("EntityManagerFactory created successfully");
            
            // Create EntityManager
            em = emf.createEntityManager();
            System.out.println("EntityManager created successfully");
            
            // Initialize DAOs with transaction support
            BookingDAO bookingDAO = new BookingDAO(em);
            TicketDAO ticketDAO = new TicketDAO(em);
            UserDAO userDAO = new UserDAO(em);
            EventDAO eventDAO = new EventDAO(em);
            BookingTicketDAO bookingTicketDAO = new BookingTicketDAO(em);
            System.out.println("DAOs initialized successfully");

            // Initialize schema modifier
            MySQLSchemaModifier schemaModifier = new MySQLSchemaModifier(em);
            System.out.println("Schema modifier initialized successfully");

            // Execute schema modifications
            try {
                System.out.println("\nExecuting schema modifications...");
                
                // Example modifications
                long duration = schemaModifier.modifySchema("add_booking_metadata");
                System.out.println("Schema modification completed in " + duration + " ms");

                
                // Print schema modification metrics
                Map<String, Object> metrics = schemaModifier.getMetrics();
                System.out.println("\nSchema Modification Metrics:");
                metrics.forEach((key, value) -> 
                    System.out.printf("%-25s: %s%n", key, value));
                
            } catch (Exception e) {
                System.err.println("Schema modification failed: " + e.getMessage());
            }

            // Initialize the BookingService
            BookingService bookingService = new BookingService( em );
            System.out.println("BookingService initialized successfully");

            // Create simulation instance
            BookingSimulation simulation = new BookingSimulation(
                bookingService, 
                userDAO, 
                eventDAO, 
                ticketDAO
            );
            System.out.println("BookingSimulation created successfully");

            // Run simulation
            System.out.println("\nStarting simulation for Event ID: 2");
            simulation.runSimulation(2);
            
            // Keep console open
            System.out.println("\nSimulation completed. Press Enter to exit...");
            System.in.read();

        } catch (Exception e) {
            System.err.println("Error during execution:");
            e.printStackTrace();
            try {
                System.out.println("Press Enter to exit...");
                System.in.read();
            } catch (Exception ex) {
                // ignore
            }
        } finally {
            // Clean up resources
            if (em != null && em.isOpen()) {
                try {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    em.close();
                    System.out.println("EntityManager closed successfully");
                } catch (Exception e) {
                    System.err.println("Error closing EntityManager: " + e.getMessage());
                }
            }
            if (emf != null && emf.isOpen()) {
                try {
                    emf.close();
                    System.out.println("EntityManagerFactory closed successfully");
                } catch (Exception e) {
                    System.err.println("Error closing EntityManagerFactory: " + e.getMessage());
                }
            }
        }
    }
}