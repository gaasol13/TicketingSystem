package com.poortoys.examples.dao;

import com.poortoys.examples.entities.Booking;
import com.poortoys.examples.entities.BookingStatus;
import com.poortoys.examples.entities.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class BookingDAO {

    private EntityManager em;

    public BookingDAO(EntityManager em) {
        this.em = em;
    }

    //creates a new booking in the database
    public void create(Booking booking) {
        em.persist(booking);
    }

    // Finds a booking by its ID
    public Booking findById(int bookingId) {
        return em.find(Booking.class, bookingId);
    }

    // Finds all bookings associated with a specific user ID
    public List<Booking> findByUserId(int userId) {
        TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.user.userId = :userId", Booking.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }
    
    public List<Booking> findAll() {
        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b", Booking.class);
        return query.getResultList();
    }

    // Updates an existing booking
    public Booking update(Booking booking) {
        return em.merge(booking);
    }
    
    public Booking findConfirmedBookingByUser(User user) {
        try {
            TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.user = :user AND b.bookingStatus = :status " +
                "ORDER BY b.bookingId DESC", // Get the most recent booking
                Booking.class);
            query.setParameter("user", user);
            query.setParameter("status", BookingStatus.CONFIRMED);
            query.setMaxResults(1); // Limit to one result
            
            List<Booking> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            System.err.println("Error finding confirmed booking for user: " + user.getUserName());
            e.printStackTrace();
            return null;
        }
    }


    // Deletes a booking
    public void delete(Booking booking) {
        em.remove(em.contains(booking) ? booking : em.merge(booking));
    }
    public Long count() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(tc) FROM Booking tc", Long.class);
        return query.getSingleResult();
    }
}
