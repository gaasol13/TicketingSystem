package com.poortoys.examples.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.*;

import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.BookingTicketDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.entities.Booking;
import com.poortoys.examples.entities.BookingStatus;
import com.poortoys.examples.entities.BookingTicket;
import com.poortoys.examples.entities.Ticket;
import com.poortoys.examples.entities.TicketCategory;
import com.poortoys.examples.entities.TicketStatus;
import com.poortoys.examples.entities.User;

/**
 * Service class handling ticket booking operations using JPA/MySQL.
 * Demonstrates MySQL's capabilities in transaction management and schema modifications.
 */
public class BookingService {

	private final EntityManager em;
    private final UserDAO userDAO;
    private final TicketDAO ticketDAO;
    private final BookingDAO bookingDAO;
    private final BookingTicketDAO bookingTicketDAO;
    private final EventDAO eventDAO;

    // Metrics fields
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);
    private long totalQueryTime = 0;
    private int totalQueries = 0;
    private long totalTransactionTime = 0;
    private int totalTransactions = 0;
    private final Map<String, Integer> errorTypes = new ConcurrentHashMap<>();

    public BookingService(EntityManager em, UserDAO userDAO, TicketDAO ticketDAO,
                         BookingDAO bookingDAO, BookingTicketDAO bookingTicketDAO, 
                         EventDAO eventDAO) {
        this.em = em;
        this.userDAO = userDAO;
        this.ticketDAO = ticketDAO;
        this.bookingDAO = bookingDAO;
        this.bookingTicketDAO = bookingTicketDAO;
        this.eventDAO = eventDAO;
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

    public Booking createBooking(int userId, List<String> ticketSerialNumbers, 
                               String deliveryEmail) throws Exception {
        EntityTransaction transaction = em.getTransaction();
        List<Ticket> lockedTickets = new ArrayList<>();
        long queryStartTime = System.nanoTime();

        try {
            transaction.begin();
            User user = findAndValidateUser(userId);
            lockedTickets = lockAndValidateTickets(ticketSerialNumbers);
            BigDecimal totalPrice = calculateTotalPrice(lockedTickets);
            
            Booking booking = createBookingEntity(user, deliveryEmail, totalPrice);
            persistBookingAndUpdateTickets(booking, lockedTickets);

            transaction.commit();
            successfulBookings.incrementAndGet();
            return booking;

        } catch (Exception e) {
            handleTransactionError(transaction, e);
            throw e;
        } finally {
            recordQueryTime(queryStartTime);
        }
    }

    private User findAndValidateUser(int userId) {
        long startTime = System.nanoTime();
        User user = em.find(User.class, userId);
        recordQueryTime(startTime);

        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }
        return user;
    }

    private List<Ticket> lockAndValidateTickets(List<String> serialNumbers) throws Exception {
        List<Ticket> lockedTickets = new ArrayList<>();

        for (String serial : serialNumbers) {
            long startTime = System.nanoTime();
            try {
                Ticket ticket = lockTicket(serial);
                validateTicketCategory(ticket);
                updateTicketStatus(ticket, TicketStatus.RESERVED);
                lockedTickets.add(ticket);
            } catch (NoResultException e) {
                throw new Exception("Ticket not available: " + serial);
            } catch (PessimisticLockException e) {
                throw new Exception("Concurrent access detected for ticket: " + serial);
            } finally {
                recordQueryTime(startTime);
            }
        }
        return lockedTickets;
    }

    private Ticket lockTicket(String serial) {
        TypedQuery<Ticket> query = em.createQuery(
                "SELECT t FROM Ticket t " +
                "LEFT JOIN FETCH t.ticketCategory tc " +
                "WHERE t.serialNumber = :serial AND t.status = :status",
                Ticket.class)
                .setParameter("serial", serial)
                .setParameter("status", TicketStatus.AVAILABLE)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setHint("javax.persistence.lock.timeout", 5000);
        return query.getSingleResult();
    }

    private void validateTicketCategory(Ticket ticket) {
        if (ticket.getTicketCategory() == null) {
            throw new RuntimeException("Ticket " + ticket.getSerialNumber() + 
                                     " has no category assigned");
        }
    }

    private void updateTicketStatus(Ticket ticket, TicketStatus status) {
        ticket.setStatus(status);
        em.merge(ticket);
    }

    private BigDecimal calculateTotalPrice(List<Ticket> tickets) {
        return tickets.stream()
                .map(ticket -> ticket.getTicketCategory().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Booking createBookingEntity(User user, String deliveryEmail, 
                                      BigDecimal totalPrice) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setDeliveryAddressEmail(deliveryEmail);
        booking.setBookingTime(new Date());
        booking.setTotalPrice(totalPrice);
        booking.setDiscount(BigDecimal.ZERO);
        booking.setFinalPrice(totalPrice);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        
        em.persist(booking);
        em.flush();
        return booking;
    }

    private void persistBookingAndUpdateTickets(Booking booking, List<Ticket> tickets) {
        for (Ticket ticket : tickets) {
            createBookingTicketAssociation(booking, ticket);
            updateTicketAsSold(ticket);
        }
        em.flush();
    }

    private void createBookingTicketAssociation(Booking booking, Ticket ticket) {
        BookingTicket bookingTicket = new BookingTicket();
        bookingTicket.setBooking(booking);
        bookingTicket.setTicket(ticket);
        em.persist(bookingTicket);
    }

    private void updateTicketAsSold(Ticket ticket) {
        ticket.setStatus(TicketStatus.SOLD);
        ticket.setPurchaseDate(new Date());
        em.merge(ticket);
    }

    private void handleTransactionError(EntityTransaction transaction, Exception e) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
        failedBookings.incrementAndGet();
    }

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
}