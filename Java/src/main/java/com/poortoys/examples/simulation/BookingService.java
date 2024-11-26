package com.poortoys.examples.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.*;

import com.poortoys.examples.entities.Booking;
import com.poortoys.examples.entities.BookingStatus;
import com.poortoys.examples.entities.BookingTicket;
import com.poortoys.examples.entities.Ticket;
import com.poortoys.examples.entities.TicketStatus;
import com.poortoys.examples.entities.User;

public class BookingService {
    // Essential fields
    private final EntityManager em;
    
    // Metrics tracking
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);
    private final AtomicInteger totalTicketsBooked = new AtomicInteger(0);
    private long totalQueryTime = 0;
    private int totalQueries = 0;

    public BookingService(EntityManager em) {
        this.em = em;
        verifyDatabaseConnection();
    }

    private void verifyDatabaseConnection() {
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            System.out.println("Database connection verified in BookingService.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify database connection", e);
        }
    }

    public List<String> getAvailableTicketSerials(int eventId) {
        long startTime = System.nanoTime();
        try {
            return em.createQuery(
                    "SELECT t.serialNumber FROM Ticket t " +
                    "WHERE t.event.eventId = :eventId AND t.status = :status", 
                    String.class)
                    .setParameter("eventId", eventId)
                    .setParameter("status", TicketStatus.AVAILABLE)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error getting available tickets: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            recordQueryTime(startTime);
        }
    }
    
    

    public synchronized Booking createBooking(int userId, List<String> ticketSerials, String email) {
        EntityTransaction tx = em.getTransaction();
        long startTime = System.nanoTime();
        totalTicketsBooked.addAndGet(ticketSerials.size());

        try {
            tx.begin();

            // Find and validate user
            User user = em.find(User.class, userId);
            if (user == null) {
                throw new RuntimeException("User not found: " + userId);
            }

            // Lock and validate tickets
            List<Ticket> tickets = new ArrayList<>();
            BigDecimal totalPrice = BigDecimal.ZERO;

            for (String serial : ticketSerials) {
                Ticket ticket = em.createQuery(
                        "SELECT t FROM Ticket t " +
                        "LEFT JOIN FETCH t.ticketCategory tc " +
                        "WHERE t.serialNumber = :serial AND t.status = :status",
                        Ticket.class)
                        .setParameter("serial", serial)
                        .setParameter("status", TicketStatus.AVAILABLE)
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .setHint("javax.persistence.lock.timeout", 5000)
                        .getSingleResult();

                if (ticket.getTicketCategory() == null) {
                    throw new RuntimeException("Ticket has no category: " + serial);
                }

                totalPrice = totalPrice.add(ticket.getTicketCategory().getPrice());
                ticket.setStatus(TicketStatus.SOLD);
                ticket.setPurchaseDate(new Date());
                tickets.add(ticket);
                em.merge(ticket);
            }

            // Create booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setDeliveryAddressEmail(email);
            booking.setBookingTime(new Date());
            booking.setTotalPrice(totalPrice);
            booking.setDiscount(BigDecimal.ZERO);
            booking.setFinalPrice(totalPrice);
            booking.setBookingStatus(BookingStatus.CONFIRMED);

            em.persist(booking);

            // Create booking-ticket associations
            for (Ticket ticket : tickets) {
                BookingTicket bookingTicket = new BookingTicket();
                bookingTicket.setBooking(booking);
                bookingTicket.setTicket(ticket);
                em.persist(bookingTicket);
            }

            em.flush();
            tx.commit();
            
            successfulBookings.incrementAndGet();
            return booking;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            failedBookings.incrementAndGet();
            throw new RuntimeException("Booking failed: " + e.getMessage(), e);
        } finally {
            recordQueryTime(startTime);
        }
    }
    
    

    // Helper method to record query time for metrics
    private void recordQueryTime(long startTime) {
        long endTime = System.nanoTime();
        totalQueryTime += (endTime - startTime);
        totalQueries++;
    }

    // Getter methods for metrics
    public int getSuccessfulBookings() {
        return successfulBookings.get();
    }

    public int getFailedBookings() {
        return failedBookings.get();
    }

    public double getAverageQueryTime() {
        return totalQueries > 0 ? (double) totalQueryTime / totalQueries / 1_000_000 : 0;
    }

    public int getTotalQueries() {
        return totalQueries;
    }
    
    public List<Ticket> lockTickets(List<String> serials) {
        return em.createQuery(
            "SELECT t FROM Ticket t WHERE t.serialNumber IN :serials AND t.status = :status",
            Ticket.class)
            .setParameter("serials", serials)
            .setParameter("status", TicketStatus.AVAILABLE)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .getResultList();
    }
    

}