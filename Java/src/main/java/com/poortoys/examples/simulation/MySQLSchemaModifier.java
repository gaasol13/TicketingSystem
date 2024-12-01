/**
 * MySQLSchemaModifier class performs schema modifications in a MySQL database using JPA.
 * It executes native SQL queries for schema changes and tracks performance and success metrics.
 */

package com.poortoys.examples.simulation;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MySQLSchemaModifier {
    // EntityManager for executing database operations
    private final EntityManager em;

    // Metrics for schema modification performance tracking
    private long totalSchemaModTime = 0; // Accumulated time for schema modifications in nanoseconds
    private int totalSchemaMods = 0; // Total number of schema modifications performed
    private final AtomicInteger successfulMods = new AtomicInteger(0); // Counter for successful modifications
    private final AtomicInteger failedMods = new AtomicInteger(0); // Counter for failed modifications

    /**
     * Constructor initializes the schema modifier with an EntityManager.
     * @param em EntityManager for database interaction.
     */
    public MySQLSchemaModifier(EntityManager em) {
        this.em = em;
    }

    /**
     * Modifies the schema based on the specified operation.
     * Executes a native SQL query and tracks success or failure.
     * @param operation The schema modification operation (e.g., "add_user_columns").
     * @return The duration of the schema modification in milliseconds.
     */
    public long modifySchema(String operation) {
        EntityTransaction transaction = em.getTransaction(); // Start a database transaction
        long startTime = System.nanoTime(); // Start timing the schema modification

        try {
            transaction.begin(); // Begin the transaction
            System.out.println("Starting MySQL schema modification: " + operation);

            // Generate the appropriate SQL command for the operation
            String sql = generateSQL(operation);
            System.out.println("Executing SQL: " + sql);
            
            em.createNativeQuery(sql).executeUpdate(); // Execute the SQL command
            transaction.commit(); // Commit the transaction
            
            // Update metrics for successful modification
            successfulMods.incrementAndGet();
            long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert duration to milliseconds
            totalSchemaModTime += duration;
            totalSchemaMods++;
            
            System.out.println("Schema modification completed in " + duration + " ms");
            return duration;

        } catch (Exception e) {
            System.err.println("Schema modification failed: " + e.getMessage());
            failedMods.incrementAndGet(); // Increment failure counter
            if (transaction.isActive()) {
                transaction.rollback(); // Rollback the transaction on failure
            }
            throw new RuntimeException("Schema modification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Generates the SQL command for the specified schema modification operation.
     * @param operation The schema modification operation name.
     * @return A string representing the SQL command.
     * @throws IllegalArgumentException if the operation is not recognized.
     */
    private String generateSQL(String operation) {
        // Define SQL commands for known operations
        switch (operation.toLowerCase()) {
            case "add_user_columns":
                return "ALTER TABLE users " +
                       "ADD COLUMN confirmation_code2 VARCHAR(100), " +
                       "ADD COLUMN confirmation_time2 DATETIME";
            case "drop_user_columns":
                return "ALTER TABLE users " +
                       "DROP COLUMN confirmation_code2, " +
                       "DROP COLUMN confirmation_time2";
            case "add_booking_metadata":
                return "ALTER TABLE bookings " +
                       "ADD COLUMN processing_time5 TIMESTAMP, " +
                       "ADD COLUMN payment_method5 VARCHAR(50), " +
                       "ADD COLUMN booking_source5 VARCHAR(50)";
            case "drop_booking_metadata":
                return "ALTER TABLE bookings " +
                       "DROP COLUMN processing_time5, " +
                       "DROP COLUMN payment_method5, " +
                       "DROP COLUMN booking_source5";
            case "modify_ticket_structure":
                return "ALTER TABLE tickets " +
                       "ADD COLUMN seat_features4 JSON, " +
                       "ADD COLUMN price_adjustment4 DECIMAL(10,2)";
            default:
                // Throw an exception for unknown operations
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    /**
     * Retrieves metrics for schema modification performance and results.
     * @return A map containing metrics such as success count, failure count, and average time.
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>(); // Create a map to store metrics
        metrics.put("successful_modifications", successfulMods.get()); // Total successful modifications
        metrics.put("failed_modifications", failedMods.get()); // Total failed modifications
        metrics.put("average_modification_time_ms", 
            totalSchemaMods > 0 ? (double) totalSchemaModTime / totalSchemaMods : 0); // Average modification time
        metrics.put("total_modifications", totalSchemaMods); // Total modifications performed
        return metrics; // Return the metrics map
    }
}
