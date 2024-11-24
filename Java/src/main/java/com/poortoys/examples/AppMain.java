package com.poortoys.examples;

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
            System.out.println("Starting Ticketing System Simulation...");
            // Create EntityManagerFactory
            emf = Persistence.createEntityManagerFactory("ticketingsystem");
            System.out.println("EntityManagerFactory created successfully");
            
            // Create EntityManager
            em = emf.createEntityManager();
            System.out.println("EntityManager created successfully");
            
            // Test initial connection
			/*
			 * em.getTransaction().begin(); Object result =
			 * em.createNativeQuery("SELECT 1").getSingleResult();
			 * em.getTransaction().commit();
			 * System.out.println("Database connection test successful");
			 */
            
            // Initialize DAOs with transaction support
            BookingDAO bookingDAO = new BookingDAO(em);
            TicketDAO ticketDAO = new TicketDAO(em);
            UserDAO userDAO = new UserDAO(em);
            EventDAO eventDAO = new EventDAO(em);
            BookingTicketDAO bookingTicketDAO = new BookingTicketDAO(em);
            System.out.println("DAOs initialized successfully");

            // Initialize the BookingService
            BookingService bookingService = new BookingService(
                em,
                userDAO,
                ticketDAO,
                bookingDAO,
                bookingTicketDAO,
                eventDAO
            );
            System.out.println("BookingService initialized successfully");

            // Create simulation instance
            BookingSimulation simulation = new BookingSimulation(bookingService, userDAO, eventDAO, ticketDAO);
            System.out.println("BookingSimulation created successfully");

            // Run simulation
            System.out.println("\nStarting simulation for Event ID: 1");
            simulation.runFullSimulation(1);//Parameters: eventId ID of the event for which the simulation is run
            
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


/*
 * I'd like to know the
 * 
 * 
 * I'd like to standardize my AppMain to work with both scenarios and enhance the displayed metrics (in the simulation class) with the following information:


Event Name: Instead of the event ID, display the actual event name.

Venue: Show the venue where the event will take place.

Total Tickets by Category: Display the total number of tickets available for each category of the event.

Total Tickets Booked by Category: Show the number of tickets already booked for each category.

Total Tickets Available: Display the total number of tickets still available across all categories.

Total Ticket Categories:
 */