package com.poortoys.examples;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class AppMain {

    public static void main(String[] args) {
    	
        try {
            MongoClient mongoClient = MongoDBConnection.getMongoClient();

            // Get a specific database
            MongoDatabase database = mongoClient.getDatabase("admin");

            // Get a specific collection
            MongoCollection<Document> collection = database.getCollection("ticketsystem");
            System.out.println("Welcome to Collection: " + collection);

            // Just to verify the connection, you can try to count the documents:
            long count = collection.countDocuments();
            System.out.println("Number of documents in the collection: " + count);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } 
        
      }
  }
