package com.poortoys.examples.dao;

import dev.morphia.Datastore;
import com.ticketing.system.entities.Genre;
import java.util.List;

public class GenreDAO {
	
	private final Datastore datastore;
	
	public GenreDAO(Datastore datastore) {
		this.datastore = datastore;
	}
	
	//Find  a genre by its name
	
	public Genre findByName(String name) {
		return datastore.find(Genre.class) //asl for the genre class
				.filter(dev.morphia.query.experimental.filters.Filters.eq("genreName", name)) // retrieves the genre name
				.first();
	}
	
	//Create a new genre
	public void create(Genre genre) {
		datastore.save(genre);
	}
	
	//Counts the total number of genres
	public long count() {
		return datastore.find(Genre.class).count();
		}
	
	   /**
     * Find all genres
     * @return List of all genres
     */
    public List<Genre> findAll() {
        return datastore.find(Genre.class).iterator().toList();
    }
    
    /**
     * Check if a genre with the given name exists
     * @param name The name to check
     * @return true if the genre exists, false otherwise
     */
    public boolean exists(String name) {
        return findByName(name) != null;
    }

}
