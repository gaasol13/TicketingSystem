package com.poortoys.examples;

import com.mongodb.client.MongoCollection;
import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.initializer.DataInitializer;
import com.ticketing.system.entities.Event;
import com.ticketing.system.simulation.BookingService;
import com.ticketing.system.simulation.BookingSimulation;

import dev.morphia.Datastore;

import org.bson.Document;
import org.bson.types.ObjectId;

public class AppMain {

	public static void main(String[] args) {
        // Create an instance of DataInitializer
        DataInitializer dataInitializer = new DataInitializer();

        try {
            // Get necessary DAOs and Datastore
            Datastore datastore = dataInitializer.getDatastore();
            BookingDAO bookingDAO = dataInitializer.getBookingDAO();
            UserDAO userDAO = dataInitializer.getUserDAO();
            EventDAO eventDAO = dataInitializer.getEventDAO();
            TicketDAO ticketDAO = dataInitializer.getTicketDAO();

            // Specify the event ID for simulation
            ObjectId eventId = new ObjectId("673133acaa85ed04a55c969d");
            
            // Verify event exists
            Event event = eventDAO.findById(eventId);
            if (event == null) {
                System.err.println("Error: Event not found with ID: " + eventId);
                return;
            }
            
            System.out.println("Starting simulation for event: " + event.getName());

            // Simulation parameters
            int numberOfUsers = 5;        // Number of concurrent users
            int maxTicketsPerUser = 4;    // Maximum tickets per booking

            // Create and run simulation
            BookingSimulation simulation = new BookingSimulation(
                datastore, 
                bookingDAO, 
                userDAO, 
                eventDAO, 
                ticketDAO
            );

            // Run both positive and negative scenarios
            System.out.println("\nRunning MongoDB Scenarios Simulation");
            System.out.println("====================================");
            System.out.println("Parameters:");
            System.out.println("- Concurrent Users: " + numberOfUsers);
            System.out.println("- Max Tickets Per User: " + maxTicketsPerUser);
            System.out.println("- Event: " + event.getName());
            System.out.println("====================================\n");

            simulation.runSimulation(eventId, numberOfUsers, maxTicketsPerUser);

        } catch (Exception e) {
            System.err.println("Simulation failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure proper cleanup
            if (dataInitializer != null) {
                System.out.println("\nCleaning up resources...");
                dataInitializer.close();
                System.out.println("MongoDB connection closed");
            }
        }
    }
}
