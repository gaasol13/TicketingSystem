package com.poortoys.examples.simulation;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MySQLSchemaModifier {
    private final EntityManager em;
    private long totalSchemaModTime = 0;
    private int totalSchemaMods = 0;
    private final AtomicInteger successfulMods = new AtomicInteger(0);
    private final AtomicInteger failedMods = new AtomicInteger(0);

    public MySQLSchemaModifier(EntityManager em) {
        this.em = em;
    }

    public long modifySchema(String operation) {
        EntityTransaction transaction = em.getTransaction();
        long startTime = System.nanoTime();

        try {
            transaction.begin();
            System.out.println("Starting MySQL schema modification: " + operation);

            String sql = generateSQL(operation);
            System.out.println("Executing SQL: " + sql);
            
            em.createNativeQuery(sql).executeUpdate();
            transaction.commit();
            
            successfulMods.incrementAndGet();
            long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert to ms
            totalSchemaModTime += duration;
            totalSchemaMods++;
            
            System.out.println("Schema modification completed in " + duration + " ms");
            return duration;

        } catch (Exception e) {
            System.err.println("Schema modification failed: " + e.getMessage());
            failedMods.incrementAndGet();
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Schema modification failed: " + e.getMessage(), e);
        }
    }

    private String generateSQL(String operation) {
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
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("successful_modifications", successfulMods.get());
        metrics.put("failed_modifications", failedMods.get());
        metrics.put("average_modification_time_ms", 
            totalSchemaMods > 0 ? (double)totalSchemaModTime / totalSchemaMods : 0);
        metrics.put("total_modifications", totalSchemaMods);
        return metrics;
    }
}