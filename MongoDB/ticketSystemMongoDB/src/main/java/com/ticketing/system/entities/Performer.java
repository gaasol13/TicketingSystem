package com.ticketing.system.entities;

import java.util.List;

import org.bson.*;
import org.bson.types.ObjectId; // for mongo ObjectId type

import dev.morphia.annotations.Entity; //to define a MongoDB collection mapping
import dev.morphia.annotations.Id;	//to mark the primary key field
import dev.morphia.annotations.Indexed; //for indexing fields
import dev.morphia.annotations.Property; //tomap class fields to document fields
import dev.morphia.annotations.Reference;


@Entity("performers")
public class Performer {
	
	@Id
	private ObjectId id; //MongoDBs unique identifier
	
	@Indexed(options = @dev.morphia.annotations.IndexOptions(unique = true, name = "performerName_idx"))//Creates a unique iindex
	@Property("performerName")//map this field to the "perfomers" field in Mongo
	private String performerName;
	
	//references the genres collection
	@Reference(lazy = true)// lazy loading improves performance by loading the reference only when accessed
	@Indexed(options = @dev.morphia.annotations.IndexOptions(name = "genreId_idx"))//Creates a unique iindex
	private List<Genre> genres; //genre id associated with the performer
	
	
	
	public Performer() {
	}

	//Constructor to initialize a Performer with a name and genre ID
	public Performer(String performerName, List<Genre> genres) {
		this.performerName = performerName;
		this.genres = genres;
	}

	public ObjectId getId() {
		return id;
	}


	public String getPerformerName() {
		return performerName;
	}

	public void setPerformerName(String performerName) {
		this.performerName = performerName;
	}
	
	

	public List<Genre> getGenres() {
		return genres;
	}

	public void setGenres(List<Genre> genres) {
		this.genres = genres;
	}

	// toString method for debugging purposes
	@Override
	public String toString() {
		return "Performer [id=" + id + ", performerName=" + performerName + ", genres=" + genres + "]";
	}
	
	
	
	
}
