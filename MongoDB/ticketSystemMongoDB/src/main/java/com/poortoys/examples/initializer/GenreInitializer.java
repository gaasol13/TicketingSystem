package com.poortoys.examples.initializer;

import java.util.List;

import com.poortoys.examples.dao.GenreDAO;
import com.ticketing.system.entities.Genre;

/*
 * Is reponsible for populating the genres collection in MongoDB
 * with a predefined list of genre names. 
 */

public class GenreInitializer implements Initializer{
	private GenreDAO genreDAO;
	private List<String> genreNames;

	public GenreInitializer(GenreDAO genreDAO, List<String> genreNames) {
        this.genreDAO = genreDAO;
        this.genreNames = genreNames;
    }
	
	//Populates the genres collection by adding genres that do not already exists
	@Override
	public void initialize() {
		System.out.println("Initializing genres...");
		for(String name : genreNames) {
			// check if the genre already exists
			if(genreDAO.findByName(name) == null ) {
				Genre genre = new Genre(name); // Create a new genre instance
				genreDAO.create(genre);
				System.out.println("Added genre: " + name);
			}else {
				System.out.println("Genre already exists: " + name);
			}
		}
		System.out.println("Genres initialization completed.\n");
	}
	
}
