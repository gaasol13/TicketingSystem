package com.poortoys.examples;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {

    public static MongoClient mongoClient;

    static {
        try {
            // Connection string with URL-encoded password
            String connectionString = "mongodb://localhost:27017/";

            // Create a MongoClient using the connection string
            mongoClient = MongoClients.create(connectionString);
        } catch (Exception e) {
            System.err.println("An error occurred while connecting to MongoDB:");
            e.printStackTrace();
        }
    }

    // Getter for the MongoClient instance
    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    // Optionally, provide a method to close the connection
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoClient connection closed.");
        }
    }
}