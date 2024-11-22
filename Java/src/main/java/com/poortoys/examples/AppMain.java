package com.poortoys.examples;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.BookingTicketDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.GenreDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.initilizer.DataInitializer;
import com.poortoys.examples.simulation.BookingService;
import com.poortoys.examples.simulation.BookingSimulation;

//import com.example.entities.Genre;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class AppMain {

    public static void main(String[] args) {
    	// Initialize JPA EntityManagerFactory and EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ticketingsystem");
        EntityManager em = emf.createEntityManager();

        // Initialize DAOs with the EntityManager
        BookingDAO bookingDAO = new BookingDAO(em);
        TicketDAO ticketDAO = new TicketDAO(em);
        UserDAO userDAO = new UserDAO(em);
        EventDAO eventDAO = new EventDAO(em);
        BookingTicketDAO bookingTicketDAO = new BookingTicketDAO(em);  

        // Initialize the BookingService with the correct parameter order
        BookingService bookingService = new BookingService(
            em,
            userDAO,
            ticketDAO,
            bookingDAO,
            bookingTicketDAO,
            eventDAO
        );
        

     // Create an instance of BookingSimulation with all required parameters
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        AtomicInteger successfulBookings = new AtomicInteger(0);
        AtomicInteger failedBookings = new AtomicInteger(0);
      
        
     // Parameters for simulation
        int eventId = 1; // Replace with the actual event ID
        int numberOfUsers = 5; // Simulate 5 users
        int maxTicketsPerUser = 4; // Each user can book up to 4 tickets
        
                
        BookingSimulation simulation = new BookingSimulation(bookingService, userDAO);
        // Run the booking simulation for the specified event
        simulation.runFullSimulation(eventId);
        // Close the EntityManager and EntityManagerFactory to release resources
        em.close();
        emf.close();
    }

}