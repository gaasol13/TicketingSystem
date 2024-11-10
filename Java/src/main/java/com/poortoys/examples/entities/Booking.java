package com.poortoys.examples.entities;

import javax.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Represents a booking made by a user for tickets.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    // Primary key of the bookings table, auto-generated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private int bookingId;

    // Many bookings can belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "delivery_address_email", length = 100)
    private String deliveryAddressEmail;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "delivery_time")
    private Date deliveryTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time_paid")
    private Date timePaid;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time_sent")
    private Date timeSent;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @Convert(converter = BookingStatusConverter.class)
    @Column(name = "booking_status", nullable = false, length = 15)
    private BookingStatus bookingStatus = BookingStatus.IN_PROGRESS;

    // One booking can have many booking_ticket associations
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingTicket> bookingTickets  = new ArrayList<>();

    // Default constructor required by JPA
    public Booking() {
    }

    public Booking(User user, String deliveryAddressEmail, Date deliveryTime, Date timePaid, Date timeSent, 
            BigDecimal totalPrice, BigDecimal discount, BigDecimal finalPrice, BookingStatus bookingStatus) {
 this.user = user;
 this.deliveryAddressEmail = deliveryAddressEmail;
 this.deliveryTime = deliveryTime;
 this.timePaid = timePaid;
 this.timeSent = timeSent;
 this.totalPrice = totalPrice;
 this.discount = discount;
 this.finalPrice = finalPrice;
 this.bookingStatus = bookingStatus;
}

    public int getBookingId() {
        return bookingId;
    }

    // Getter and setter for user
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Getter and setter for deliveryAddressEmail
    public String getDeliveryAddressEmail() {
        return deliveryAddressEmail;
    }

    public void setDeliveryAddressEmail(String deliveryAddressEmail) {
        this.deliveryAddressEmail = deliveryAddressEmail;
    }

    // Getter and setter for deliveryTime
    public Date getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Date deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    // Getter and setter for timePaid
    public Date getTimePaid() {
        return timePaid;
    }

    public void setTimePaid(Date timePaid) {
        this.timePaid = timePaid;
    }

    // Getter and setter for timeSent
    public Date getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(Date timeSent) {
        this.timeSent = timeSent;
    }

    // Getter and setter for totalPrice
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    // Getter and setter for discount
    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    // Getter and setter for finalPrice
    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    // Getter and setter for bookingStatus
    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    // Getter and setter for bookingTickets
    public List<BookingTicket> getBookingTickets() {
        return bookingTickets;
    }

    public void setBookingTickets(List<BookingTicket> bookingTickets) {
        this.bookingTickets = bookingTickets;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", user=" + (user != null ? user.getUserName() : "null") +
                ", deliveryAddressEmail='" + deliveryAddressEmail + '\'' +
                ", deliveryTime=" + deliveryTime +
                ", timePaid=" + timePaid +
                ", timeSent=" + timeSent +
                ", totalPrice=" + totalPrice +
                ", discount=" + discount +
                ", finalPrice=" + finalPrice +
                ", bookingStatus=" + bookingStatus +
                '}';
    }


    
}
