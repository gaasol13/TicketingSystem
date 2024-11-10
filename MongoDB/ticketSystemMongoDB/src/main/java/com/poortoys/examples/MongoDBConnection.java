package com.poortoys.examples;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoDBConnection {

/*	private static MongoClient mongoClient; 
	private static final String
	CONNECTION_STRING = "mongodb://localhost:27017/"; 
	private static final String
	DATABASE_NAME = "ticketsystem"; 
	private static final String COLLECTION_NAME =
			"genres";

	static { 
		try { // Create a MongoClient using the connection string
			mongoClient = MongoClients.create(CONNECTION_STRING); 
		} catch (Exception e) {
			System.err.println("An error occurred while connecting to MongoDB:");
			e.printStackTrace(); } }

	// Method to retrieve the MongoCollection directly 
	public static MongoCollection<Document> getCollection() {
		MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
		return database.getCollection(COLLECTION_NAME); 
	}

	// Close the MongoDB client connection 
	public static void close() { 
		if	(mongoClient != null) { 
			mongoClient.close();

			System.out.println("MongoClient connection closed."); 
		} */
	

}
