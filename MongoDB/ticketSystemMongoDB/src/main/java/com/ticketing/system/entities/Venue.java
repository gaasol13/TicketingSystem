package com.ticketing.system.entities;

import org.bson.types.ObjectId;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Property;

@Entity("venues")
public class Venue {
	
	@Id
	private ObjectId id;//mongos unique identifier
	
	@Indexed(options = @dev.morphia.annotations.IndexOptions(unique = true, name = "venueName_idx"))
	@Property("venue_name")//map this field to the venue field in Mongo
	private String venueName;
	
	@Property("address")
	private String address;
	
	@Property("type")
	private String type;
	
	@Property("capacity")
	private int capacity;
	

	public Venue() {
	}



	public Venue(String venueName, String address, String type, int capacity) {
		this.venueName = venueName;
		this.address = address;
		this.type = type;
		this.capacity = capacity;
	}


	public ObjectId getId() {
		return id;
	}


	public String getVenueName() {
		return venueName;
	}


	public void setVenueName(String venueName) {
		this.venueName = venueName;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public int getCapacity() {
		return capacity;
	}


	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}



	@Override
	public String toString() {
		return "Venue [id=" + id + ", venueName=" + venueName + ", location=" + address + ", type=" + type
				+ ", capacity=" + capacity + "]";
	}
	
	
	

}
