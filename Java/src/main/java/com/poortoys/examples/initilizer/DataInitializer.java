package com.poortoys.examples.initilizer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.mindrot.jbcrypt.BCrypt;

import com.poortoys.examples.entities.Genre;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.*;


/*
 * DataInitializer is responsible for populating the database with sample data/
 */
public class DataInitializer {
	
	//NAme of the persistence unit defined in persistence.xml
	private static final String PERSISTENCE_UNIT_NAME ="ticketingsystem";
	//EntittyManagerFactory and EntityManager for interacting with persistence.xml
	private EntityManagerFactory emf;
	private EntityManager em;
	
	//Constructor
	public DataInitializer() {
		//Create and EntityManagerFactory based on the persistence unit
		emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		// Create an EntityManager to manage entities
		em = emf.createEntityManager();
		
	}


	
	//Populates the database with sample data within a transaction
	
	
	public void populateData() {
		try {
			//Begin a new transaction
			em.getTransaction().begin();
			
			//call methods ti create and persist entities
			createGenres();
			
			
			// Commit the transaction after successful operations
			em.getTransaction().commit();
			
			//Validate data insertion by counting records
			validateData();
			
		}catch (Exception e){
			//Roll back the transaction in case of any errors
			if(em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			//Print stack trace
			e.printStackTrace();
		}
		
	}
	
	//Closes the EntityManager and EntityManagerFactory 
	
	private void validateData() {
		// TODO Auto-generated method stub
		
	}
	public void close() {
		// TODO Auto-generated method stub
		if (em !=null) em.close();
		if (emf != null) emf.close();
		
	}
	
	//Genres persistence
	private void createGenres() {
		System.out.println("Creating Genres...");
		
		/*
		 * // List of genres List<Genre> genres = Arrays.asList( new Genre("Rock"), new
		 * Genre("Jazz"), new Genre("Classical"), new Genre("Pop"), new
		 * Genre("Electronic"), new Genre("Hip-Hop"), new Genre("Country"), new
		 * Genre("Blues"), new Genre("Reggae"), new Genre("Metal") );
		 */
		 
		 //Persist each Genre entity
			/*
			 * for (Genre genre : genres) { em.persist(genre); }
			 * System.out.println("Genres created");
			 */
	}

}
