package com.porrtoys.examples.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.PessimisticLockException;

import com.poortoys.examples.dao.*;
import com.poortoys.examples.entities.*;

public class BookingService {
    private final EntityManager em;
    private final UserDAO userDAO;
    private final TicketDAO ticketDAO;
    private final BookingDAO bookingDAO;
    private final BookingTicketDAO bookingTicketDAO;
    private final EventDAO eventDAO;
    
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);
    
    public BookingService(EntityManager em, UserDAO userDAO, TicketDAO ticketDAO, 
            BookingDAO bookingDAO, BookingTicketDAO bookingTicketDAO, EventDAO eventDAO) {
        this.em = em;
        this.userDAO = userDAO;
        this.ticketDAO = ticketDAO;
        this.bookingDAO = bookingDAO;
        this.bookingTicketDAO = bookingTicketDAO;
        this.eventDAO = eventDAO;
    }
    

    
    
    
    
    public List<String> getAvailableTicketSerials(int eventId) {
        try {
            return em.createQuery(
                "SELECT t.serialNumber FROM Ticket t " +
                "WHERE t.event.eventId = :eventId AND t.status = :status", String.class)
                .setParameter("eventId", eventId)
                .setParameter("status", TicketStatus.AVAILABLE)
                .getResultList();
        } catch (Exception e) {
            System.err.println("Error getting available tickets: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    
    
    
    
    
    public Booking createBooking(int userId, List<String> ticketSerialNumbers, String deliveryEmail) {
        EntityTransaction transaction = em.getTransaction();
        List<Ticket> lockedTickets = new ArrayList<>();
        
        try {
            transaction.begin();
            
            // 1. Lock and validate tickets
            for (String serialNumber : ticketSerialNumbers) {
                Ticket ticket = em.createQuery(
                    "SELECT t FROM Ticket t WHERE t.serialNumber = :serialNumber", 
                    Ticket.class)
                    .setParameter("serialNumber", serialNumber)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getSingleResult();
                
                if (ticket.getStatus() != TicketStatus.AVAILABLE) {
                    throw new RuntimeException("Ticket not available: " + serialNumber);
                }
                lockedTickets.add(ticket);
            }
            
            // If we got here, we have successfully locked all tickets
            // 2. Retrieve user
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new RuntimeException("User not found with ID: " + userId);
            }

            // 3. Get event from first ticket
            Event event = lockedTickets.get(0).getEvent();

            // 4. Calculate prices
            BigDecimal totalPrice = calculateTotalPrice(lockedTickets);
            BigDecimal discount = calculateDiscount(user, totalPrice);
            BigDecimal finalPrice = totalPrice.subtract(discount);

            // 5. Create booking
            Booking booking = new Booking();
            booking.setUser(user);
            //booking.setEvent(event);
            booking.setDeliveryAddressEmail(deliveryEmail);
            booking.setDeliveryTime(new Date());
            booking.setTimePaid(new Date());
            booking.setTotalPrice(calculateTotalPrice(lockedTickets));
            booking.setDiscount(discount);
            booking.setFinalPrice(finalPrice);
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            
            // 6. Persist booking
            bookingDAO.create(booking);

            // 7. Create booking tickets and update ticket status
            for (Ticket ticket : lockedTickets) {
                // Create booking ticket
                BookingTicket bookingTicket = new BookingTicket();
                bookingTicket.setBooking(booking);
                bookingTicket.setTicket(ticket);
                bookingTicketDAO.create(bookingTicket);

                // Update ticket status - ticket is still locked
                ticket.setStatus(TicketStatus.SOLD);
                ticket.setPurchaseDate(new Date());
                ticketDAO.update(ticket);
            }

            transaction.commit();
            successfulBookings.incrementAndGet();
            return booking;

        } catch (PessimisticLockException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            failedBookings.incrementAndGet();
            throw new RuntimeException("Could not lock all required tickets. Please try again.", e);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            failedBookings.incrementAndGet();
            throw new RuntimeException("Booking failed: " + e.getMessage(), e);
        }
    }
    
    
    

    private BigDecimal calculateTotalPrice(List<Ticket> tickets) {
        return tickets.stream()
            .map(ticket -> ticket.getTicketCategory().getPrice())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    
    

    private BigDecimal calculateDiscount(User user, BigDecimal totalPrice) {
        List<Booking> userBookings = bookingDAO.findByUserId(user.getUserId());
        if (userBookings.size() > 5) {
            return totalPrice.multiply(new BigDecimal("0.05")); // 5% discount for loyal customers
        }
        return BigDecimal.ZERO;
    }
    



    public int getSuccessfulBookings() {
        return successfulBookings.get();
    }

    public int getFailedBookings() {
        return failedBookings.get();
    }
}