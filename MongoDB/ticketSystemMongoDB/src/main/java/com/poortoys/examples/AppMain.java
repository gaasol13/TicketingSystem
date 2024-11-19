package com.poortoys.examples;

import com.mongodb.client.MongoCollection;
import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.initializer.DataInitializer;
import com.ticketing.system.simulation.BookingService;
import com.ticketing.system.simulation.BookingSimulation;

import dev.morphia.Datastore;

import org.bson.Document;
import org.bson.types.ObjectId;

public class AppMain {

    public static void main(String[] args) {
    	
    	//Create an instance of DataInitializer
    	DataInitializer dataInitializer = new DataInitializer();
    	
    	
        Datastore datastore = dataInitializer.getDatastore();
        BookingDAO bookingDAO = dataInitializer.getBookingDAO();
        UserDAO userDAO = dataInitializer.getUserDAO();
        EventDAO eventDAO = dataInitializer.getEventDAO();
        TicketDAO ticketDAO = dataInitializer.getTicketDAO();

        
        // Initialize BookingService
        BookingService bookingService = new BookingService(
            bookingDAO,
            ticketDAO,
            userDAO,
            eventDAO,
            datastore
           );
        
        // Specify the event for which to run the simulation
        // Replace with the actual event ID you want to test
        ObjectId eventId = new ObjectId("673133acaa85ed04a55c969d");

        // Parameters for simulation
        int numberOfUsers = 5; // Simulate 1000 users
        int maxTicketsPerUser = 4; // Each user can book up to 4 tickets

        BookingSimulation simulation = new BookingSimulation(datastore, bookingDAO, userDAO, eventDAO, ticketDAO);
        simulation.runSimulation(eventId, numberOfUsers, maxTicketsPerUser);

        // Close the DataInitializer
        dataInitializer.close();
		

		 
    }
}
