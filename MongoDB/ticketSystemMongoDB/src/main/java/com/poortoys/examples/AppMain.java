/**
 * AppMain class serves as the entry point for the MongoDB-based Ticketing System Simulation.
 * It initializes the system, performs schema modifications, and runs a booking simulation.
 */

package com.poortoys.examples;

// Import necessary libraries and classes for MongoDB and the simulation
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
    /**
     * The main method initializes and runs the ticketing system simulation.
     * @param args Command-line arguments (not used in this application).
     */
    public static void main(String[] args) {
        DataInitializer dataInitializer = null; // Handles system and database initialization
        
        try {
            // Step 1: Initialize the data initializer for system setup
            dataInitializer = new DataInitializer();
            System.out.println("Starting MongoDB Ticketing System Simulation...");
            
            // Step 2: Initialize system configuration (datastore, DAOs, etc.)
            dataInitializer.initializeSystemConfiguration();
            
            // Step 3: Retrieve database components from the initializer
            Datastore datastore = dataInitializer.getDatastore(); // Datastore for MongoDB operations
            BookingDAO bookingDAO = dataInitializer.getBookingDAO(); // DAO for booking operations
            UserDAO userDAO = dataInitializer.getUserDAO(); // DAO for user operations
            EventDAO eventDAO = dataInitializer.getEventDAO(); // DAO for event operations
            TicketDAO ticketDAO = dataInitializer.getTicketDAO(); // DAO for ticket operations
            
            // Step 4: Initialize the schema modifier to handle schema updates
            MongoDBSchemaModifier schemaModifier = new MongoDBSchemaModifier(datastore);
            System.out.println("Services initialized successfully");
            
            // Step 5: Execute schema modifications
            System.out.println("\nExecuting schema modifications...");
            
            // Example modification: Add metadata fields to the "bookings" collection
            boolean success = schemaModifier.modifySchema("ADD_BOOKING_METADATA", "bookings");
            System.out.println("Schema modification " + 
                (success ? "completed successfully" : "failed"));
            
            // Step 6: Print schema modification metrics
            Map<String, Object> metrics = schemaModifier.getMetrics(); // Retrieve modification metrics
            System.out.println("\nSchema Modification Metrics:");
            metrics.forEach((key, value) -> 
                System.out.printf("%-25s: %s%n", key, value)); // Print metrics in a formatted manner
            
            // Step 7: Run booking simulation for a specific event
            ClientSession session = null; // Transaction session (optional for MongoDB operations)
            ObjectId eventId = new ObjectId("674823c02f8d0a1f89ce48b0"); // ID of the event to simulate
            BookingSimulation simulation = new BookingSimulation(
                datastore, 
                bookingDAO, 
                userDAO, 
                eventDAO, 
                ticketDAO
            );
            
            // Run the simulation and process bookings
            simulation.runSimulation(session, eventId);
            
        } catch (Exception e) {
            // Handle any exceptions during execution
            System.err.println("Error during execution:");
            e.printStackTrace(); // Print the exception stack trace for debugging
        } finally {
            // Ensure resources are properly closed, including database connections
            if (dataInitializer != null) {
                try {
                    dataInitializer.close(); // Close all initialized resources
                    System.out.println("MongoDB connections closed successfully");
                } catch (Exception e) {
                    // Log any errors during resource cleanup
                    System.err.println("Error closing MongoDB connections: " + 
                        e.getMessage());
                }
            }
        }
    }
}
