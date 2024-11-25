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
    	
        // Initialize system but don't populate bookings
        DataInitializer dataInitializer = new DataInitializer();
        dataInitializer.initializeSystemConfiguration();
        
        try {
        	
            Datastore datastore = dataInitializer.getDatastore();
            BookingDAO bookingDAO = dataInitializer.getBookingDAO();
            UserDAO userDAO = dataInitializer.getUserDAO();
            EventDAO eventDAO = dataInitializer.getEventDAO();
            TicketDAO ticketDAO = dataInitializer.getTicketDAO();

            // Event ID for "Rock Fest 2024"
            ObjectId eventId = new ObjectId("67431483e28821695f08d966");
            
            // Run simulation
            BookingSimulation simulation = new BookingSimulation(
                datastore, bookingDAO, userDAO, eventDAO, ticketDAO
            );
            simulation.runSimulation(eventId);

        } finally {
            dataInitializer.close();
        }
    }
}