/**
 * BookingDAO class provides CRUD operations for Booking entities in a MongoDB database.
 * It uses Morphia for Object-Document Mapping (ODM) to facilitate database interactions.
 */

package com.poortoys.examples.dao;

// Import necessary Morphia and MongoDB libraries for database operations
import java.util.List;
import org.bson.types.ObjectId;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.updates.UpdateOperators;
import com.mongodb.session.ClientSession;
import com.ticketing.system.entities.Booking;
import dev.morphia.Datastore;

public class BookingDAO {
    // Datastore instance for MongoDB interaction using Morphia
    private final Datastore datastore;

    /**
     * Constructor to initialize the DAO with a datastore instance.
     * @param datastore The Morphia Datastore object connected to the MongoDB database.
     */
    public BookingDAO(Datastore datastore) {
        this.datastore = datastore;
    }

    /**
     * Creates a new booking in the database.
     * @param booking The Booking object to be saved.
     */
    public void create(Booking booking) {
        create(null, booking); // Calls the overloaded method to handle both with and without session
    }

    /**
     * Creates a new booking in the database with a provided ClientSession.
     * @param session The ClientSession object for managing transactions (can be null).
     * @param booking The Booking object to be saved.
     */
    public void create(ClientSession session, Booking booking) {
        if (session != null) {
            datastore.save(session); // Save using the provided session
        } else {
            datastore.save(booking); // Save directly to the database
        }
    }

    /**
     * Finds a booking by its unique ID.
     * @param id The ObjectId of the booking.
     * @return The Booking object if found, or null otherwise.
     */
    public Booking findById(ObjectId id) {
        return datastore.find(Booking.class)
                .filter(Filters.eq("_id", id)) // Match booking by ID
                .first(); // Return the first matching result
    }

    /**
     * Finds all bookings associated with a specific user ID.
     * @param userId The ObjectId of the user.
     * @return A list of Booking objects associated with the user.
     */
    public List<Booking> findByUserId(ObjectId userId) {
        return datastore.find(Booking.class)
                .filter(Filters.eq("user_id", userId)) // Match bookings by user ID
                .iterator() // Fetch the results as an iterator
                .toList(); // Convert the iterator to a list and return
    }

    /**
     * Updates an existing booking in the database.
     * @param booking The Booking object with updated details.
     */
    public void update(Booking booking) {
        datastore.save(booking); // Save the updated booking
    }

    /**
     * Finds the most recent confirmed booking for a specific user.
     * @param userId The ObjectId of the user.
     * @return The most recent confirmed Booking object, or null if none exists.
     */
    public Booking findConfirmedBookingByUser(ObjectId userId) {
        return datastore.find(Booking.class)
                .filter(
                        Filters.eq("user_id", userId), // Match bookings by user ID
                        Filters.eq("status", "booked") // Only include bookings with 'booked' status
                )
                .iterator(new FindOptions()
                        .sort(Sort.descending("_id")) // Sort by ID in descending order (most recent first)
                        .limit(1)) // Limit results to 1
                .tryNext(); // Return the first result or null if none
    }

    /**
     * Finds a booking by a ticket ID.
     * @param ticketId The ObjectId of the ticket.
     * @return The Booking object containing the ticket, or null if none exists.
     */
    public Booking findByTicketId(ObjectId ticketId) {
        return datastore.find(Booking.class)
                .filter(Filters.eq("tickets", ticketId)) // Match bookings containing the ticket ID
                .first(); // Return the first matching result
    }

    /**
     * Deletes a booking from the database.
     * @param booking The Booking object to be deleted.
     */
    public void delete(Booking booking) {
        datastore.delete(booking); // Delete the specified booking
    }

    /**
     * Counts the total number of bookings in the database.
     * @return The total number of bookings.
     */
    public long count() {
        return datastore.find(Booking.class).count(); // Count and return all bookings
    }

    /**
     * Retrieves all bookings from the database.
     * @return A list of all Booking objects.
     */
    public List<Booking> findAll() {
        return datastore.find(Booking.class).iterator().toList(); // Fetch all bookings and return as a list
    }

    /**
     * Adds tickets to an existing booking.
     * @param bookingId The ID of the booking to update.
     * @param ticketIds The list of ticket IDs to add.
     */
    public void addTicketsToBooking(ObjectId bookingId, List<ObjectId> ticketIds) {
        Query<Booking> query = datastore.find(Booking.class)
                .filter(Filters.eq("_id", bookingId)); // Match the booking by ID
        query.update(UpdateOperators.addToSet("tickets", ticketIds)) // Add tickets to the 'tickets' field
             .execute(); // Execute the update
    }

    /**
     * Removes tickets from an existing booking.
     * @param bookingId The ID of the booking to update.
     * @param ticketIds The list of ticket IDs to remove.
     */
    public void removeTicketsFromBooking(ObjectId bookingId, List<ObjectId> ticketIds) {
        Query<Booking> query = datastore.find(Booking.class)
                .filter(Filters.eq("_id", bookingId)); // Match the booking by ID
        query.update(UpdateOperators.pullAll("tickets", ticketIds)) // Remove tickets from the 'tickets' field
             .execute(); // Execute the update
    }

    /**
     * Counts the number of bookings for a user in a specific event.
     * @param userId The ID of the user.
     * @param eventId The ID of the event.
     * @return The total number of bookings for the user in the event.
     */
    public long countBookingsByUserAndEvent(ObjectId userId, ObjectId eventId) {
        return datastore.find(Booking.class)
                .filter(
                        Filters.eq("user_id", userId), // Match by user ID
                        Filters.eq("event_id", eventId) // Match by event ID
                )
                .count(); // Count and return the results
    }

    /**
     * Counts the total number of bookings for a specific user.
     * @param userId The ID of the user.
     * @return The total number of bookings for the user.
     */
    public long countBookingsByUser(ObjectId userId) {
        return datastore.find(Booking.class)
                .filter(Filters.eq("user_id", userId)) // Match by user ID
                .count(); // Count and return the results
    }
}
