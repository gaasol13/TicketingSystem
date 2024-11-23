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
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Update;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperator;
import dev.morphia.query.experimental.updates.UpdateOperators;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	 // Add this method to your TicketDAO class
	    public List<Ticket> findAvailableTickets(ObjectId eventId) {
	        return datastore.find(Ticket.class)
	            .filter(Filters.and(
	                Filters.eq("event_id", eventId),
	                Filters.eq("status", "AVAILABLE")
	            ))
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
	    public void update(ClientSession session, ObjectId ticketId, String status) {
	        Query<Ticket> query = datastore.find(Ticket.class)
	            .filter(Filters.eq("_id", ticketId));
	        UpdateOperator update = UpdateOperators.set("status", status);
	        query.update(update).execute();
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
	     */
	    public List<Ticket> bookAvailableTickets(ClientSession session, ObjectId eventId, int quantity) {
	        List<Ticket> bookedTickets = new ArrayList<>();

	        try {
	            for (int i = 0; i < quantity; i++) {
	                Ticket ticket = datastore.find(Ticket.class)
	                    .filter(Filters.and(
	                        Filters.eq("event_id", eventId),
	                        Filters.eq("status", "available")
	                    ))
	                    .first();

	                if (ticket != null) {
	                    ticket.setStatus("booked");
	                    ticket.setPurchaseDate(new Date());
	                    datastore.save(ticket);
	                    bookedTickets.add(ticket);
	                } else {
	                    break;
	                }
	            }

	            if (bookedTickets.isEmpty()) {
	                System.err.println("No available tickets found for event " + eventId);
	            }
	        } catch (Exception e) {
	            System.err.println("Failed to book tickets for event " + eventId + ": " + e.getMessage());
	            e.printStackTrace();
	            throw e;
	        }

	        return bookedTickets;
	    }
	    /**
	     * Counts the number of available tickets for a specific event.
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
	     */
	    public Ticket findById(ObjectId id) {
	        return datastore.find(Ticket.class)
	                .filter(Filters.eq("_id", id))
	                .first();
	    }
	    
	    public Ticket findAndModifyTicket(ClientSession session, ObjectId ticketId, String status) {
	        try {
	            // Create query to find available ticket by ID
	            Query<Ticket> query = datastore.find(Ticket.class)
	                .filter(Filters.and(
	                    Filters.eq("_id", ticketId),
	                    Filters.eq("status", "AVAILABLE")
	                ));

	            // Create update operation to set the new status
	            UpdateOperator updateOperator = UpdateOperators.set("status", status);

	            // Execute findAndModify operation
	            return query.modify(new FindAndModifyOptions()
	                .returnDocument(ReturnDocument.AFTER)
	                .upsert(false))
	                .update(updateOperator)
	                .execute();

	        } catch (Exception e) {
	            System.err.println("Error in findAndModifyTicket: " + e.getMessage());
	            return null;
	        }
	    }

		



}
