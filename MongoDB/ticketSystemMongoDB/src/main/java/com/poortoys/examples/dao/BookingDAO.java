package com.poortoys.examples.dao;

import java.util.List;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import dev.morphia.query.experimental.filters.Filters;

import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.Update;
import dev.morphia.query.experimental.updates.*;

import com.mongodb.client.model.Updates;
import com.ticketing.system.entities.Booking;

import dev.morphia.Datastore;

public class BookingDAO {
	
	private Datastore datastore;
	
	public BookingDAO(Datastore datastore) {
		this.datastore = datastore;
	}
	
	//Creates a new booking in the database
	public void create(Booking booking) {
		datastore.save(booking);
	}
	
	//Find by Id
	public Booking findById(ObjectId id) {
		return datastore.find(Booking.class)
				.filter(Filters.eq("_id", id))
				.first();
	}
	
	//find all boking associated with a specific user ID
	public List<Booking> findByUserId(ObjectId userId){
      return datastore.find(Booking.class)
    		  .filter(Filters.eq("user_id", userId))
    		  .iterator()
    		  .toList();
	}
	
	//updte an existing booking
	public void update(Booking booking) {
		datastore.save(booking);
	}
	
	//finds the most recent confirmed booking for a user
	public Booking findConfirmedBookingByUser(ObjectId userId) {
		return datastore.find(Booking.class)
				.filter(
						Filters.eq("user_id", userId),
						Filters.eq("status", "confirmed")
						)
				.iterator(new FindOptions()
						.sort(Sort.descending("_id"))
						.limit(1))
				.tryNext();
				
	}
	
	public Booking findByTicketId(ObjectId ticketId) {
	    return datastore.find(Booking.class)
	            .filter(Filters.eq("tickets", ticketId))
	            .first();
	}
	
	//deletes a booking
	public void delete(Booking booking) {
		datastore.delete(booking);
	}
	
	//count total bookings
	public long count() {
		return datastore.find(Booking.class).count();
	}
	
	   // Retrieves all bookings
    public List<Booking> findAll() {
        return datastore.find(Booking.class).iterator().toList();
    }
	
    // Add tickets to a booking
    public void addTicketsToBooking(ObjectId bookingId, List<ObjectId> ticketIds) {
        Query<Booking> query = datastore.find(Booking.class)
                                        .filter(Filters.eq("_id", bookingId));
        UpdateOperator updateOperator = UpdateOperators.addToSet("tickets", ticketIds);
        query.update(updateOperator).execute();
    }

    // Remove tickets from a booking
	/*
	 * public void removeTicketsFromBooking(ObjectId bookingId, List<ObjectId>
	 * ticketIds) { Query<Booking> query = datastore.find(Booking.class)
	 * .filter(Filters.eq("_id", bookingId));
	 * 
	 * // Use pullAll to remove multiple items List<UpdateOperator> update =
	 * Updates.pullAll("tickets", ticketIds); query.update(update).execute(); }
	 */
    
    // Method to count bookings per user per event
    public long countBookingsByUserAndEvent(ObjectId userId, ObjectId eventId) {
        return datastore.find(Booking.class)
                .filter(
                        Filters.eq("user_id", userId),
                        Filters.eq("event_id", eventId)
                )
                .count();
    }

}
