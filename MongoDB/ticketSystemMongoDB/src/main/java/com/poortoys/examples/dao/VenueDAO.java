package com.poortoys.examples.dao;

import java.util.List;

import com.ticketing.system.entities.Venue;

import dev.morphia.Datastore;

public class VenueDAO {
	
	//Morphias datastore for interacting with MongoDB
	private final Datastore datastore;
	
	// constructor that initializes the venueDAO with a given Datastore
	public VenueDAO() {
		this.datastore = null;
	}
	
	public VenueDAO(Datastore datastore) {
		super();
		this.datastore = datastore;
	}
	
	//find a venue by its name
	public Venue findByName(String name) {
		return datastore.find(Venue.class)
				.filter(dev.morphia.query.experimental.filters.Filters.eq("venueName",name))
				.first();
	}
	
	// Create a new venue
	public void create(Venue venue) {
		datastore.save(venue);
	}
	
	// coundt the total number of venues
	public long count() {
		return datastore.find(Venue.class).count();
	}
	
	//Find all venues
	public List<Venue> findAll(){
		return datastore.find(Venue.class).iterator().toList();
	}
	
	//Check if the performer already exists
	public boolean exists(String name) {
		return findByName(name) != null;
	}

}
