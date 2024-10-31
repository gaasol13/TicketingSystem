// File: Genre.java
package com.ticketing.system.entities;

import org.bson.types.ObjectId;

/**
 * Represents a music genre in the ticketing system.
 */
public class Genre {
    private ObjectId id; // MongoDB's unique identifier
    private String name; // Name of the genre (e.g., Rock, Jazz)

    // Default constructor
    public Genre() {
    }

    // Parameterized constructor
    public Genre(String name) {
        this.name = name;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier of the genre.
     * 
     * @return ObjectId of the genre.
     */
    public ObjectId getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the genre.
     * 
     * @param id ObjectId to set.
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * Gets the name of the genre.
     * 
     * @return Name of the genre.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the genre.
     * 
     * @param name Name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    // toString method for debugging purposes

    @Override
    public String toString() {
        return "Genre{id=" + id + ", name='" + name + "'}";
    }
}
