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
import com.mongodb.session.ClientSession;
import com.ticketing.system.entities.Booking;

import dev.morphia.Datastore;

public class BookingDAO {
    private Datastore datastore;

    public BookingDAO(Datastore datastore) {
        this.datastore = datastore;
    }

    // Creates a new booking in the database
    public void create(Booking booking) {
        create(null, booking);
    }

    // Creates a new booking in the database with a provided session
    public void create(ClientSession session, Booking booking) {
        if (session != null) {
            datastore.save(session);
        } else {
            datastore.save(booking);
        }
    }

    // Find by Id
    public Booking findById(ObjectId id) {
        return datastore.find(Booking.class)
                .filter(Filters.eq("_id", id))
                .first();
    }

    // Find all bookings associated with a specific user ID
    public List<Booking> findByUserId(ObjectId userId) {
        return datastore.find(Booking.class)
                .filter(Filters.eq("user_id", userId))
                .iterator()
                .toList();
    }

    // Update an existing booking
    public void update(Booking booking) {
        datastore.save(booking);
    }

    // Find the most recent confirmed booking for a user
    public Booking findConfirmedBookingByUser(ObjectId userId) {
        return datastore.find(Booking.class)
                .filter(
                        Filters.eq("user_id", userId),
                        Filters.eq("status", "booked")
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

    // Delete a booking
    public void delete(Booking booking) {
        datastore.delete(booking);
    }

    // Count total bookings
    public long count() {
        return datastore.find(Booking.class).count();
    }

    // Retrieve all bookings
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
    public void removeTicketsFromBooking(ObjectId bookingId, List<ObjectId> ticketIds) {
        Query<Booking> query = datastore.find(Booking.class)
                .filter(Filters.eq("_id", bookingId));
        UpdateOperator updateOperator = UpdateOperators.pullAll("tickets", ticketIds);
        query.update(updateOperator).execute();
    }

    // Method to count bookings per user per event
    public long countBookingsByUserAndEvent(ObjectId userId, ObjectId eventId) {
        return datastore.find(Booking.class)
                .filter(
                        Filters.eq("user_id", userId),
                        Filters.eq("event_id", eventId)
                )
                .count();
    }

    public long countBookingsByUser(ObjectId userId) {
        return datastore.find(Booking.class)
                .filter(
                        Filters.eq("user_id", userId)
                )
                .count();
    }
}