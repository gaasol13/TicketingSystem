/**
 * MongoDBSchemaModifier class is responsible for performing schema modifications on MongoDB collections.
 * It tracks performance metrics such as success/failure counts and average modification times.
 */

package com.ticketing.system.simulation;

// Importing necessary libraries for MongoDB operations and utility handling
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import dev.morphia.Datastore;
import org.bson.Document;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MongoDBSchemaModifier {
    // Datastore instance for MongoDB operations
    private final Datastore datastore;

    // Metrics for tracking schema modification performance
    private long totalSchemaModTime = 0; // Total time spent on schema modifications
    private int totalSchemaMods = 0;     // Total number of schema modifications attempted
    private final AtomicInteger successfulMods = new AtomicInteger(0); // Counter for successful modifications
    private final AtomicInteger failedMods = new AtomicInteger(0);     // Counter for failed modifications

    /**
     * Constructor to initialize the MongoDBSchemaModifier with a datastore instance.
     * @param datastore The Datastore object for interacting with the MongoDB database.
     */
    public MongoDBSchemaModifier(Datastore datastore) {
        this.datastore = datastore;
    }

    /**
     * Modifies the schema of a specified MongoDB collection based on the operation provided.
     * @param operation      The schema modification operation to perform (e.g., "ADD_CONFIRMATION").
     * @param collectionName The name of the collection to modify.
     * @return true if the schema modification was successful; false otherwise.
     */
    public boolean modifySchema(String operation, String collectionName) {
        System.out.println("Attempting MongoDB schema modification: " + operation);
        long startTime = System.nanoTime(); // Start timer to measure operation duration

        try {
            // Fetch the MongoDB collection where the modification will be applied
            MongoCollection<Document> collection = datastore.getDatabase()
                .getCollection(collectionName);

            // Create the modification document based on the operation specified
            Document modification = createModification(operation);

            // Apply the modification to all documents in the collection
            collection.updateMany(new Document(), modification);

            // Update metrics for a successful modification
            successfulMods.incrementAndGet();
            long duration = (System.nanoTime() - startTime); // Calculate duration of the operation
            totalSchemaModTime += duration; // Accumulate modification time
            totalSchemaMods++; // Increment the total modification count

            System.out.println("Schema modification successful: " + operation);
            return true;

        } catch (Exception e) {
            // Handle and log exceptions during the modification process
            failedMods.incrementAndGet(); // Increment the failure counter
            System.err.println("Schema modification failed: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            return false;
        }
    }

    /**
     * Creates a Document specifying the schema modification based on the operation provided.
     * @param operation The schema modification operation (e.g., "ADD_CONFIRMATION").
     * @return A Document containing the modification to be applied.
     * @throws IllegalArgumentException if the operation is not recognized.
     */
    private Document createModification(String operation) {
        // Determine the type of modification to apply based on the operation
        switch (operation) {
            case "ADD_CONFIRMATION":
                // Adds fields for confirmation details (code and time) to the schema
                return new Document("$set", new Document()
                    .append("confirmation_code", "") // Adds a blank confirmation_code field
                    .append("confirmation_time", null)); // Adds a null confirmation_time field

            case "ADD_BOOKING_METADATA":
                // Adds fields for booking metadata such as source, processing time, and payment details
                return new Document("$set", new Document()
                    .append("booking_source", "ONLINE") // Adds a default booking_source field
                    .append("processing_time", new Date()) // Adds a processing_time field with current timestamp
                    .append("payment_details", new Document())); // Adds a blank payment_details field

            case "UPDATE_TICKET_STRUCTURE":
                // Adds fields for dynamic pricing and seat features to ticket schema
                return new Document("$set", new Document()
                    .append("dynamic_pricing", new Document()) // Adds a blank dynamic_pricing field
                    .append("seat_features", new Document())); // Adds a blank seat_features field

            default:
                // Throw an exception for unrecognized operations
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    /**
     * Retrieves performance metrics for schema modifications.
     * @return A Map containing metrics such as success count, failure count, and average modification time.
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>(); // Initialize a map to store metrics
        metrics.put("successful_modifications", successfulMods.get()); // Total successful modifications
        metrics.put("failed_modifications", failedMods.get());         // Total failed modifications
        metrics.put("average_modification_time_ms", 
            totalSchemaMods > 0 ? (totalSchemaModTime / totalSchemaMods / 1_000_000.0) : 0); // Average time in ms
        metrics.put("total_modifications", totalSchemaMods);           // Total number of modifications attempted
        return metrics; // Return the metrics map
    }
}
