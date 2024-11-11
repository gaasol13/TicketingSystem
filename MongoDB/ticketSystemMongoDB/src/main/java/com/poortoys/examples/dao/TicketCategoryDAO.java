package com.poortoys.examples.dao;

import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.ticketing.system.entities.Event;
import com.ticketing.system.entities.TicketCategory;

import dev.morphia.Datastore;
import dev.morphia.query.experimental.filters.Filters;

public class TicketCategoryDAO {

    private final Datastore datastore;

    /**
     * Constructor injecting the Morphia Datastore.
     */
    public TicketCategoryDAO(Datastore datastore) {
        this.datastore = datastore;
    }

    /**
     * Finds all TicketCategories associated with a particular Event.
     */
    public Set<TicketCategory> findByEventId(ObjectId eventId) {
        // Retrieve the Event by its ObjectId
        Event event = datastore.find(Event.class)
                .filter(Filters.eq("_id", eventId))
                .first();
        // Return the list of TicketCategories if Event exists
        return event != null ? event.getTicketCategories() : null;
    }

    /**
     * Adds a new TicketCategory to an existing Event.
     */
    public void addTicketCategory(ObjectId eventId, TicketCategory ticketCategory) {
    	 // Retrieve the Event by its ObjectId
        Event event = datastore.find(Event.class)
                .filter(Filters.eq("_id", eventId))
                .first();
        if (event != null) {
            // Attempt to add the TicketCategory to the Set
            boolean added = event.getTicketCategories().add(ticketCategory);
            if (added) {
                // Save the updated Event back to the database
                datastore.save(event);
                System.out.println("Added ticket category: " + ticketCategory.getDescription() + " to event: " + event.getName());
            } else {
                System.out.println("Ticket category already exists: " + ticketCategory.getDescription() + " for event: " + event.getName());
            }
        } else {
            System.out.println("Event with ID " + eventId + " not found. Cannot add TicketCategory: " + ticketCategory.getDescription());
        }
    }

    /**
     * Removes a TicketCategory from an Event.
     */
    public void removeTicketCategory(ObjectId eventId, TicketCategory ticketCategory) {
        // Retrieve the Event by its ObjectId
        Event event = datastore.find(Event.class)
                .filter(Filters.eq("_id", eventId))
                .first();
        if (event != null) {
            // Remove the specified TicketCategory from the Event's list
            event.getTicketCategories().remove(ticketCategory);
            // Save the updated Event back to the database
            datastore.save(event);
        }
    }

    /**
     * Updates a TicketCategory within an Event.
     */
    public void updateTicketCategory(ObjectId eventId, TicketCategory ticketCategory) {
        // Retrieve the Event by its ObjectId
        Event event = datastore.find(Event.class)
                .filter(Filters.eq("_id", eventId))
                .first();
        if (event != null && event.getTicketCategories() != null) {
            // Remove the old TicketCategory
            event.getTicketCategories().remove(ticketCategory);
            // Add the updated TicketCategory
            event.addTicketCategory(ticketCategory);
            // Save the updated Event back to the database
            datastore.save(event);
            System.out.println("Updated ticket category: " + ticketCategory.getDescription() + " for event: " + event.getName());
        }
    }

    /**
     * Counts the total number of TicketCategories across all Events.
     */
    public long count() {
        // Retrieve all Events and sum their TicketCategories
        return datastore.find(Event.class)
                .iterator()
                .toList()
                .stream()
                .mapToLong(event -> event.getTicketCategories().size())
                .sum();
    }
}