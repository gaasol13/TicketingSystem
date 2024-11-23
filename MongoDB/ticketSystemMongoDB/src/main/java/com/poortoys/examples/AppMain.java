package com.poortoys.examples;

import com.mongodb.client.MongoCollection;
import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.initializer.DataInitializer;
import com.ticketing.system.simulation.BookingSimulation;

import dev.morphia.Datastore;

import org.bson.Document;
import org.bson.types.ObjectId;

public class AppMain {

    public static void main(String[] args) {
    	
    	//Create an instance of DataInitializer
    	DataInitializer dataInitializer = new DataInitializer();
    	
    	//dataInitializer.populateData();
        Datastore datastore = dataInitializer.getDatastore();
        BookingDAO bookingDAO = dataInitializer.getBookingDAO();
        UserDAO userDAO = dataInitializer.getUserDAO();
        EventDAO eventDAO = dataInitializer.getEventDAO();
        TicketDAO ticketDAO = dataInitializer.getTicketDAO();

        // Specify the event for which to run the simulation
        // Replace with the actual event ID you want to test
        ObjectId eventId = new ObjectId("673133acaa85ed04a55c96a1");

        // Parameters for simulation
        int numberOfUsers = 100; // Simulate 1000 users
        int maxTicketsPerUser = 4; // Each user can book up to 4 tickets

        BookingSimulation simulation = new BookingSimulation(datastore, bookingDAO, userDAO, eventDAO, ticketDAO);
        simulation.runSimulation(eventId);

        // Close the DataInitializer
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