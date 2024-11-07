package com.poortoys.examples.dao;

import com.poortoys.examples.entities.BookingTicket;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * DAO class for managing BookingTicket entities.
 * Provides methods to create and retrieve booking-ticket associations.
 */
public class BookingTicketDAO {

    private final EntityManager em;

    /**
     * Constructor to initialize EntityManager.
     */
    public BookingTicketDAO(EntityManager em) {
        this.em = em;
    }

    /**
     * Persists a new BookingTicket entity in the database.
     */
    public void create(BookingTicket bookingTicket) {
        em.persist(bookingTicket);
    }

    /**
     * Finds all BookingTicket entities associated with a specific booking ID.
     *
     * @param bookingId The ID of the booking
     * @return List of BookingTicket entities associated with the booking ID
     */
    public List<BookingTicket> findByBookingId(int bookingId) {
        TypedQuery<BookingTicket> query = em.createQuery(
            "SELECT bt FROM BookingTicket bt WHERE bt.booking.bookingId = :bookingId", BookingTicket.class);
        query.setParameter("bookingId", bookingId);
        return query.getResultList();
    }

    /**
     * Retrieves all BookingTicket records.
     */
    public List<BookingTicket> findAll() {
        TypedQuery<BookingTicket> query = em.createQuery("SELECT bt FROM BookingTicket bt", BookingTicket.class);
        return query.getResultList();
    }
    
    
    
    public List<BookingTicket> findByTicketId(int ticketId) {
        TypedQuery<BookingTicket> query = em.createQuery(
            "SELECT bt FROM BookingTicket bt WHERE bt.ticket.ticketId = :ticketId", 
            BookingTicket.class);
        query.setParameter("ticketId", ticketId);
        return query.getResultList();
    }

    /**
     * Counts the total number of BookingTicket entities in the database.
     */
    public Long count() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(bt) FROM BookingTicket bt", Long.class);
        return query.getSingleResult();
    }
}
