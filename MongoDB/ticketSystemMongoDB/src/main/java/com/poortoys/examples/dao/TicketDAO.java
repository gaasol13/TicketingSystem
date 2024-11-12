package com.poortoys.examples.dao;

import java.util.List;

import org.bson.types.ObjectId;

import com.ticketing.system.entities.Ticket;

import dev.morphia.Datastore;
import dev.morphia.query.experimental.filters.Filters;

public class TicketDAO {
	
	   private final Datastore datastore;

	    public TicketDAO(Datastore datastore) {
	        this.datastore = datastore;
	    }

	    // Find all tickets for a specific event
	    public List<Ticket> findByEventId(ObjectId eventId) {
	        return datastore.find(Ticket.class)
	                .filter(Filters.eq("event_id", eventId))
	                .iterator()
	                .toList();
	    }

	    // Find all available tickets for a specific event
	    public List<Ticket> findAvailableTicketsByEventId(ObjectId eventId) {
	        return datastore.find(Ticket.class)
	                .filter(
	                        Filters.eq("event_id", eventId),
	                        Filters.eq("status", "available")
	                )
	                .iterator()
	                .toList();
	    }
	    
	    // Find tickets by their IDs
	    public List<Ticket> findByIds(List<ObjectId> ticketIds) {
	        return datastore.find(Ticket.class)
	                .filter(Filters.in("_id", ticketIds))
	                .iterator()
	                .toList();
	    }

	    // Find a ticket by its serial number
	    public Ticket findBySerialNumber(String serialNumber) {
	        return datastore.find(Ticket.class)
	                .filter(Filters.eq("serial_number", serialNumber))
	                .first();
	    }

	    // Find all tickets
	    public List<Ticket> findAll() {
	        return datastore.find(Ticket.class)
	                .iterator()
	                .toList();
	    }

	    // Persist a new ticket into the database
	    public void create(Ticket ticket) {
	        datastore.save(ticket);
	    }

	    // Update an existing ticket in the database
	    public void update(Ticket ticket) {
	        datastore.save(ticket);
	    }

	    // Delete a ticket from the database
	    public void delete(Ticket ticket) {
	        datastore.delete(ticket);
	    }

	    // Count the total number of tickets
	    public long count() {
	        return datastore.find(Ticket.class).count();
	    }

}
