package com.poortoys.examples.initializer;

import java.util.Arrays;
import java.util.List;


import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;



import org.bson.Document;

import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.GenreDAO;
import com.poortoys.examples.dao.PerformerDAO;
import com.poortoys.examples.dao.TicketCategoryDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.dao.VenueDAO;
import com.ticketing.system.entities.Genre;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

/**
 * Responsible for populating the MongoDB genres collection with initial sample data.
 * It creates and persists Genre entities, ensuring that duplicate entries are not inserted.
 */
public class DataInitializer {
    
    // Morphia's Datastore for interacting with MongoDB
    private final Datastore datastore;
    
    // DAO instance for Genre entity
    private final GenreDAO genreDAO;
    private final PerformerDAO performerDAO;
    private final VenueDAO venueDAO;
    private final EventDAO eventDAO;
    private final TicketCategoryDAO ticketCategoryDAO;
    private final UserDAO userDAO;
    
    // List of initializer instances
    private final List<Initializer> initializers;
    
    // MongoClient instance to manage the connection lifecycle
    private final MongoClient mongoClient;
    
    /**
     * Constructor that initializes the Datastore and DAOs.
     * It also sets up the list of initializers to populate data.
     */
    public DataInitializer() {
        // Initialize MongoDB client
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        
        // Create Datastore and configure entity mapping
        datastore = Morphia.createDatastore(mongoClient, "ticketsystem");
        datastore.getMapper().mapPackage("dev.morphia.example");
        
        // Ensure indexes are created
        datastore.ensureIndexes();
        
        // Initialize GenreDAO with the Datastore
        genreDAO = new GenreDAO(datastore);
        performerDAO = new PerformerDAO(datastore);
        venueDAO = new VenueDAO(datastore);
        eventDAO = new EventDAO(datastore);
        ticketCategoryDAO = new TicketCategoryDAO(datastore);
        userDAO = new UserDAO(datastore);
        
        // Initialize the list of initializers (only GenreInitializer)
        initializers = Arrays.asList(
            new GenreInitializer(genreDAO, getGenreNames()),
            new PerformerInitializer(performerDAO, genreDAO),
        	new VenueInitializer(venueDAO),
        	new EventInitializer(eventDAO, performerDAO, venueDAO),
        	new TicketCategoryInitializer(ticketCategoryDAO, eventDAO),
        	new UserInitializer(userDAO)
        );
    }
    
    /**
     * Populates the DB with initial data by executing each initializer.
     */
    public void populateData() {
        try {
            for (Initializer initializer : initializers) {
                initializer.initialize();
            }
            validateData();
        } catch (Exception e) {
            System.err.println("An error occurred during data initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Validates data insertion by checking the count of genres.
     */
    private void validateData() {
        System.out.println("Validating data insertion...");
        System.out.println("Total Genres: " + genreDAO.count());
        System.out.println("Total performers: " + performerDAO.count());
        System.out.println("Total venues: " + venueDAO.count());
        System.out.println("Total events: " + eventDAO.count());
        System.out.println("Total categories: " + ticketCategoryDAO.count());
        System.out.println("Total users: " + userDAO.count());
    }
    
    /**
     * Retrieves the list of genre names to populate.
     * @return List of genre names
     */
    private List<String> getGenreNames() {
        return Arrays.asList(
            "Rock", "Jazz", "Classical", "Pop", "Electronic",
            "Hip-Hop", "Country", "Blues", "Reggae", "Metal"
        );
    }
    
    /**
     * Closes the MongoDB connection and Datastore resources.
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed");
        }
    }
    
    /**
     * Gets the Datastore instance.
     * @return The Morphia Datastore
     */
    public Datastore getDatastore() {
        return datastore;
    }
    
    /**
     * Gets the GenreDAO instance.
     * @return The GenreDAO
     */
    public GenreDAO getGenreDAO() {
        return genreDAO;
    }
    
    public PerformerDAO getPerformerDAO() {
        return performerDAO;
    }
    
    public VenueDAO getVenueDAO() {
        return venueDAO;
    }
    
    public EventDAO getEventDAO() {
        return eventDAO;
    }
    
    public TicketCategoryDAO getTicketCategoryDAO() {
    	return ticketCategoryDAO;
    }
}