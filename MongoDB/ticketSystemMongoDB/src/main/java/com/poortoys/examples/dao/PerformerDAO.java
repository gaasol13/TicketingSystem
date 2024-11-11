package com.poortoys.examples.dao;

import java.util.List;

import com.ticketing.system.entities.Performer;

import dev.morphia.Datastore;
import dev.morphia.query.experimental.filters.Filters;

public class PerformerDAO {
	
	//morphias datastore for interacting with mongoDB
	private final Datastore datastore;
	
	
	//Constructor that initializes the PErformerDAO with a given Datastore
	public PerformerDAO() {
		this.datastore = null;

	}
	
	
	
	public PerformerDAO(Datastore datastore) {
		super();
		this.datastore = datastore;
	}



	//find a performer by its name
	public Performer findByName(String name) {
		return datastore.find(Performer.class) // ask and search for the performer clss
				.filter(dev.morphia.query.experimental.filters.Filters.eq("performerName", name))
				.first();
	}
	
	
    public List<Performer> findByNames(List<String> names) {
        return datastore.find(Performer.class)
                .filter(Filters.in("performerName", names))
                .iterator()
                .toList();
    }
    
	//Create a new genre
	public void create(Performer performer) {
		datastore.save(performer);
	}
	
	//Count the total number of performers
	public long count() {
		return datastore.find(Performer.class).count();
	}
	
	//find all performers
	public List<Performer> findAll(){
		return datastore.find(Performer.class).iterator().toList();
	}
	
	//check if the perfomer already exists
	public boolean exists(String name) {
		return findByName(name) != null;
	}

}
