package com.poortoys.examples.entities;

import javax.persistence.*;

/**
 * Represents the association between a Booking and a Ticket.
 * This entity captures which tickets are included in a particular booking.
 */

@Entity
@Table(name = "booking_ticket")
public class BookingTicket {

    // Primary key of the booking_ticket table, auto-generated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_ticket_id")
    private int bookingTicketId;

    // Many booking tickets belong to one booking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Many booking tickets are associated with one ticket
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    // Default constructor required by JPA
    public BookingTicket() {
    }

    /**
     * Constructor to create a BookingTicket with associated Booking and Ticket.
     *
     * @param booking the booking associated with this booking ticket
     * @param ticket  the ticket associated with this booking ticket
     */
    public BookingTicket(Booking booking, Ticket ticket) {
        this.booking = booking;
        this.ticket = ticket;
    }

    // Getter for bookingTicketId (no setter since it's auto-generated)
    public int getBookingTicketId() {
        return bookingTicketId;
    }

    // Getter and setter for booking
    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    // Getter and setter for ticket
    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    // Optional: Override toString() for better readability
    @Override
    public String toString() {
        return "BookingTicket{" +
                "bookingTicketId=" + bookingTicketId +
                ", bookingId=" + (booking != null ? booking.getBookingId() : "null") +
                ", ticketId=" + (ticket != null ? ticket.getTicketId() : "null") +
                '}';
    }
}
