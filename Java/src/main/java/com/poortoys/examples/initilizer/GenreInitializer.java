package com.poortoys.examples.initilizer;
import java.util.List;

import com.poortoys.examples.dao.GenreDAO;
import com.poortoys.examples.entities.Genre;

public class GenreInitializer implements Initializer{
	private GenreDAO genreDAO;
	private List<String> genreNames;
	
	/*
	 * Constructor
	 * @param genreDAO Data access object for Genre entity
	 * @param genreNames List of genre names to populate
	 */
	public GenreInitializer(GenreDAO genreDAO, List<String> genreNames) {
		super();
		this.genreDAO = genreDAO;
		this.genreNames = genreNames;
	}
	
	/*
	 * Initialize genres by iterating through the provided list,
	 * Checking for existing entries, and persisting new genres
	 */
	
	@Override
	public void initialize() {
		System.out.println("Initializing genres>>>>");
		for (String name : genreNames) {
			if(genreDAO.findByName(name) == null) {
				Genre genre = new Genre(name);
				genreDAO.create(genre);
				System.out.println("Added genre: " + name);
			}else {
				System.out.println("Genre already exists: " + name);
			}
		}
		System.out.println("Genres initialization completed.\n");
	}
	
	
	
	

}


