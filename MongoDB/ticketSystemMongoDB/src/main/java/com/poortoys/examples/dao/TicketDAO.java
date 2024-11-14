package com.poortoys.examples.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.ticketing.system.entities.Ticket;

import dev.morphia.Datastore;
import dev.morphia.FindAndModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;

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
	    
	    /**
	     * Atomically finds and updates available tickets to 'booked' status.
	     *
	     * @param session The client session for transaction management.
	     * @param eventId The ID of the event for which tickets are being booked.
	     * @param quantity The number of tickets to book.
	     * @return A list of booked Ticket objects.
	     */
	    @SuppressWarnings("removal")
		public List<Ticket> bookAvailableTickets(ClientSession session, ObjectId eventId, int quantity) {
	        List<Ticket> bookedTickets = new ArrayList<>();

	        for (int i = 0; i < quantity; i++) {
	            Query<Ticket> query = datastore.find(Ticket.class)
	                .filter(Filters.eq("eventId", eventId))
	                .filter(Filters.eq("status", "available"));

	            UpdateOperations<Ticket> updateOps = datastore.createUpdateOperations(Ticket.class)
	                .set("status", "booked")
	                .set("purchaseDate", new Date());

	            try {
	                Ticket ticket = datastore.findAndModify(
	                    query,
	                    updateOps,
	                    new FindAndModifyOptions()
	                        .returnNew(true));

	                if (ticket != null) {
	                    bookedTickets.add(ticket);
	                } else {
	                    break; // No more available tickets
	                }
	            } catch (Exception e) {
	                // Log the error and continue
	                System.err.println("Failed to book ticket for event " + eventId + ": " + e.getMessage());
	                continue;
	            }
	        }

	        return bookedTickets;
	    }
	    /**
	     * Counts the number of available tickets for a specific event.
	     *
	     * @param eventId The ID of the event.
	     * @return The count of available tickets.
	     */
	    public long countAvailableTickets(ObjectId eventId) {
	        return datastore.find(Ticket.class)
	                .filter(
	                        Filters.eq("event_id", eventId),
	                        Filters.eq("status", "available")
	                )
	                .count();
	    }

	    // Other existing methods...
	    
	    /**
	     * Finds a User by their ObjectId.
	     *
	     * @param userId The ObjectId of the user.
	     * @return The User object, or null if not found.
	     */
	    public Ticket findById(ObjectId id) {
	        return datastore.find(Ticket.class)
	                .filter(Filters.eq("_id", id))
	                .first();
	    }

}
