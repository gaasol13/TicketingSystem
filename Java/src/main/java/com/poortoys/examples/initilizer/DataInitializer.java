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
	private UserDAO userDAO;
	private EventDAO eventDAO;
	private TicketCategoryDAO ticketCategoryDAO;
	private TicketDAO ticketDAO;
	/*
	 * private PerformerDAO performerDAO; private VenueDAO venueDAO; private
	 * EventDAO eventDAO; private TicketCategoryDAO ticketCategoryDAO; private
	 * TicketDAO ticketDAO; ; private BookingDAO bookingDAO;
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
		performerDAO = new PerformerDAO(em);
		venueDAO = new VenueDAO(em);
		 userDAO = new UserDAO(em);
		eventDAO = new EventDAO(em);
		ticketCategoryDAO = new TicketCategoryDAO(em);
		 ticketDAO = new TicketDAO(em);
		
		
		
		// Initialize initializers
        initializers = new ArrayList<>();
		// Initialize initializers
		List<String> genreNames = new ArrayList<>();
		
		initializers.add(new GenreInitializer(genreDAO, genreNames));
		

		initializers.add(new PerformerInitializer(performerDAO, genreDAO));
		
		// Add venue initializer
        initializers.add(new VenueInitializer(venueDAO));
        
     // Add user initializer
        initializers.add(new UserInitializer(userDAO));
        
        initializers.add(new EventInitializer(eventDAO, performerDAO, venueDAO));
        initializers.add(new TicketCategoryInitializer(ticketCategoryDAO, eventDAO));
        initializers.add(new TicketInitializer(ticketDAO, eventDAO, ticketCategoryDAO));
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



	//Validates the data insertion
	private void validateData() {
		System.out.println("Validating...Chill out");
		System.out.println("Total Genres: " + genreDAO.count());
		System.out.println("Total Performers: " + performerDAO.count());
		 System.out.println("Total Venues: " + venueDAO.count());
		 System.out.println("Total Users: " + userDAO.count());
		 
		System.out.println("Total Events: " + eventDAO.count());
		System.out.println("Total Ticket Categories: " + ticketCategoryDAO.count());
		System.out.println("Total Ticket Categories: " + ticketDAO.count());

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


}
