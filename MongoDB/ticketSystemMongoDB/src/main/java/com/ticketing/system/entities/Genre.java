// File: Genre.java
package com.ticketing.system.entities;

import org.bson.types.ObjectId;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Property;

/**
 * Represents a music genre in the ticketing system.
 */

@Entity("genres")
public class Genre {
	
	@Id 
    private ObjectId id; // MongoDB's unique identifier
	
	@Indexed(options = @dev.morphia.annotations.IndexOptions(unique = true))//Creates a unique index on genreName
	@Property("genreName") // Maps this field to the "genreNAme field in mongoDB
    private String genreName; // Name of the genre (e.g., Rock, Jazz)

	
    // Default constructor
    public Genre() {
    }

    // Parameterized constructor
    public Genre(String genreName) {
        this.genreName = genreName;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier of the genre.
     */
    public ObjectId getId() {
        return id;
    }

    /**
     * Gets the name of the genre.
     */
    public String getGenreName() {
        return genreName;
    }
    
    public void setGenreName(String genreName) { // Renamed from setName to setGenreName
        this.genreName = genreName;
    }

    /**
     * Sets the name of the genre.
     * 
     * @param name Name to set.
     */
    public void setName(String name) {
        this.genreName = name;
    }

    // toString method for debugging purposes

    @Override
    public String toString() {
        return "Genre{id=" + id + ", genreName='" + genreName + "'}";
    }
}
