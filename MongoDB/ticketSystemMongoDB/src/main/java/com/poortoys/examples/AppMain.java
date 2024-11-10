package com.poortoys.examples;

import com.mongodb.client.MongoCollection;
import com.poortoys.examples.initializer.DataInitializer;

import org.bson.Document;

public class AppMain {

    public static void main(String[] args) {
    	
    	//Create an instance of DataInitializer
    	DataInitializer dataInitializer = new DataInitializer();
    	
    	//Populate data into mongo
    	dataInitializer.populateData();
    	
    	//close the datastore
    	dataInitializer.close();
    	
		
		/*
		 * try { // Directly retrieve the collection from MongoDBConnection
		 * MongoCollection<Document> collection = MongoDBConnection.getCollection();
		 * System.out.println("Welcome to Collection: " + collection);
		 * 
		 * // Verify the connection by counting documents long count =
		 * collection.countDocuments();
		 * System.out.println("Number of documents in the collection: " + count);
		 * 
		 * } catch (Exception e) { System.err.println("Error: " + e.getMessage());
		 * e.printStackTrace(); } finally { // Ensure the MongoDB client closes properly
		 * MongoDBConnection.close(); }
		 */
		 
    }
}
