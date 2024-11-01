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

	/*
	 * private PerformerDAO performerDAO; private VenueDAO venueDAO; private
	 * EventDAO eventDAO; private TicketCategoryDAO ticketCategoryDAO; private
	 * TicketDAO ticketDAO; private UserDAO userDAO; private BookingDAO bookingDAO;
	 * private BookingTicketDAO bookingTicketDAO;
	 */
    
    //Initializer the instances
    private List<Initializer> initializers;
    
    
    
    //Constructor that initializes the EntityManagerFactory, EntityManager, and DAOs.
     
    public DataInitializer() {
        // Create an EntityManagerFactory based on the persistence unit
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        // Create an EntityManager to manage entities
        em = emf.createEntityManager();

        // Initialize DAOs with the EntityManager
        genreDAO = new GenreDAO(em);
		/*
		 * performerDAO = new PerformerDAO(em); venueDAO = new VenueDAO(em); eventDAO =
		 * new EventDAO(em); ticketCategoryDAO = new TicketCategoryDAO(em); ticketDAO =
		 * new TicketDAO(em); userDAO = new UserDAO(em); bookingDAO = new
		 * BookingDAO(em); bookingTicketDAO = new BookingTicketDAO(em);
		 */
        
        // Initialize initializers
      List<String> genreNames = Arrays.asList(
    		  "Techno Duro", "Psy Trance");
      
      initializers = Arrays.asList(
              new GenreInitializer(genreDAO, genreNames));
    }
    
    
    
    /*
     * populates the database with initial data by executing each initializer 
     * within its own transaction.
    */
    
    public void populateData() {
    	try {
    		
    		for (Initializer initializer : initializers) {
    			em.getTransaction().begin();
    			initializer.initialize();
    			em.getTransaction().commit();
    		}
            
            validateData();
            
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
   
    
     // Closes the EntityManager and EntityManagerFactory to release resources
     
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

    
    
    //Validates the data insertion
    private void validateData() {
    	System.out.println("Validating...Chill out");
    	System.out.println("Total Genres: " + genreDAO.count());
    }

	
    
  


}
