package com.poortoys.examples;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.poortoys.examples.dao.*;
import com.poortoys.examples.initializer.DataInitializer;
import com.ticketing.system.simulation.BookingSimulation;
import com.ticketing.system.simulation.MongoDBSchemaModifier;
import dev.morphia.Datastore;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Map;

public class AppMain {
    public static void main(String[] args) {
        DataInitializer dataInitializer = null;
        
        try {
            // Initialize system
            dataInitializer = new DataInitializer();
            System.out.println("Starting MongoDB Ticketing System Simulation...");
            
            // Initialize system configuration
            dataInitializer.initializeSystemConfiguration();
            
            // Get database components
            Datastore datastore = dataInitializer.getDatastore();
            BookingDAO bookingDAO = dataInitializer.getBookingDAO();
            UserDAO userDAO = dataInitializer.getUserDAO();
            EventDAO eventDAO = dataInitializer.getEventDAO();
            TicketDAO ticketDAO = dataInitializer.getTicketDAO();
            
            // Initialize schema modifier
            MongoDBSchemaModifier schemaModifier = new MongoDBSchemaModifier(datastore);
            System.out.println("Services initialized successfully");
            
            // Execute schema modifications
            System.out.println("\nExecuting schema modifications...");
            
            // Example modifications
            boolean success = schemaModifier.modifySchema("ADD_BOOKING_METADATA", "bookings");
            System.out.println("Schema modification " + 
                (success ? "completed successfully" : "failed"));
            
            // Print schema modification metrics
            Map<String, Object> metrics = schemaModifier.getMetrics();
            System.out.println("\nSchema Modification Metrics:");
            metrics.forEach((key, value) -> 
                System.out.printf("%-25s: %s%n", key, value));
            
            // Run simulation with specific event ID
            ClientSession session = null;
            ObjectId eventId = new ObjectId("674823c02f8d0a1f89ce48b1");
            BookingSimulation simulation = new BookingSimulation(
                datastore, 
                bookingDAO, 
                userDAO, 
                eventDAO, 
                ticketDAO
            );
            
            simulation.runSimulation(session, eventId);
            
        } catch (Exception e) {
            System.err.println("Error during execution:");
            e.printStackTrace();
        } finally {
            if (dataInitializer != null) {
                try {
                    dataInitializer.close();
                    System.out.println("MongoDB connections closed successfully");
                } catch (Exception e) {
                    System.err.println("Error closing MongoDB connections: " + 
                        e.getMessage());
                }
            }
        }
    }
}