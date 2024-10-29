package com.poortoys.examples.initilizer;


import com.poortoys.examples.dao.*;
import com.poortoys.examples.entities.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * The DataInitializer class is responsible for populating the database with sample data.
 * It creates and persists entities such as Genres, Performers, Venues, Events,
 * TicketCategories, Tickets, Users, Bookings, and BookingTickets.
 */
public class DataInitializer {

    // The name of the persistence unit defined in persistence.xml
    private static final String PERSISTENCE_UNIT_NAME = "ticketingsystem";

    // EntityManagerFactory and EntityManager for interacting with the persistence context
    private EntityManagerFactory emf;
    private EntityManager em;

    // DAO instances for each entity
    private GenreDAO genreDAO;
    private PerformerDAO performerDAO;
    private VenueDAO venueDAO;
    private EventDAO eventDAO;
    private TicketCategoryDAO ticketCategoryDAO;
    private TicketDAO ticketDAO;
    private UserDAO userDAO;
    private BookingDAO bookingDAO;
    private BookingTicketDAO bookingTicketDAO;

    /**
     * Constructor that initializes the EntityManagerFactory, EntityManager, and DAOs.
     */
    public DataInitializer() {
        // Create an EntityManagerFactory based on the persistence unit
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        // Create an EntityManager to manage entities
        em = emf.createEntityManager();

        // Initialize DAOs with the EntityManager
        genreDAO = new GenreDAO(em);
        performerDAO = new PerformerDAO(em);
        venueDAO = new VenueDAO(em);
        eventDAO = new EventDAO(em);
        ticketCategoryDAO = new TicketCategoryDAO(em);
        ticketDAO = new TicketDAO(em);
        userDAO = new UserDAO(em);
        bookingDAO = new BookingDAO(em);
        bookingTicketDAO = new BookingTicketDAO(em);
    }
    
    /*
     * Populate the database with sample data within a transaction
     * It calls methods to create and persiste each entity type
     * This method ensure atomicity, if any insertuon fails, all changes are rolled back
     * 
     */
    
    public void populateData() {
    	try {
    		// Begin transaction
    		em.getTransaction().begin();
    		
    		// Create and persist Genres
    		createGenres();
    		
    		//Commit transaction after 
    		em.getTransaction().commit();
            System.out.println("Transaction committed successfully.");
    	} catch (Exception e) {
    		//Rollback transaction in case of any errors
    		if (em.getTransaction().isActive()) {
    			em.getTransaction().rollback();
    			System.out.println("Transaction rolled back due to an error.");
    		}
    		// Print stack trace for debugging
    		e.printStackTrace();
    	}
    }
    
    /*
     * Closes the EntityManager and EntityManagerFactory to release resources
     * 
     */
    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
            System.out.println("EntityManager closed.");
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("EntityManagerFactory closed.");
        }
    }

    
    /*
     * Create and persists Genre Entities
     * It avoids duplication by checking if a genre already exists before inserting
     */
    private void createGenres() {
    	System.out.println("Creating the genres, please wait :)");
    	
    	//List of genre names to insert
    	List<String> genreNames = Arrays.asList(
    			"Rock", "Jazz", "Dub", "Classical",
    			"Techno", "Drum and Bass", "Hip-Hop",
    			"Blues", "Reggae", "Electroswing"
    			);
    	
    	//Iterate through the list of genres
    	for(String name : genreNames) {
    		//Check if the genre already exists
    		Genre existingGenre = genreDAO.findByName(name);
    		if (existingGenre == null) {
    			//If not, create and persist the new Genre
    			Genre genre = new Genre(name);
    			genreDAO.create(genre);
    			System.out.println("Genre added: " + name);
    		} else {
    			//If exists, skip to prevent duplication
    			System.out.println("Genre already exists: " + name);
    		}
    	}
    	
    	System.out.println("Genres creation completed.\n");
    
    }


}
