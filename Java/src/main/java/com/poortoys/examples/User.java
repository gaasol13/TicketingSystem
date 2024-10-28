package com.poortoys.examples;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Represents a user interacting with the ticketing system.
 */
@Entity
@Table(name = "users")
public class User {

    // Primary key of the users table, auto-generated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int userId;

    // Unique username for login, cannot be null, maximum length of 50 characters
    @Column(name = "user_name", unique = true, nullable = false, length = 50)
    private String userName;

    // Unique email address for the user, cannot be null, maximum length of 100 characters
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    // Hashed password for secure authentication, cannot be null, maximum length of 255 characters
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // Code for account verification, optional, maximum length of 100 characters
    @Column(name = "confirmation_code", length = 100)
    private String confirmationCode;

    // Timestamp of when the account was confirmed, optional
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "confirmation_time")
    private Date confirmationTime;

    // One user can have many bookings
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;

    // Default constructor required by JPA
    public User() {
    }

    // Constructor for convenience
    public User(String userName, String email, String passwordHash) {
        this.userName = userName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Getter for userId (no setter since it's auto-generated)
    public int getUserId() {
        return userId;
    }

    // Getter and setter for userName
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // Getter and setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and setter for passwordHash
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // Getter and setter for confirmationCode
    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    // Getter and setter for confirmationTime
    public Date getConfirmationTime() {
        return confirmationTime;
    }

    public void setConfirmationTime(Date confirmationTime) {
        this.confirmationTime = confirmationTime;
    }

    // Getter and setter for bookings
    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    // Optional: Override toString() for better readability
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", confirmationCode='" + confirmationCode + '\'' +
                ", confirmationTime=" + confirmationTime +
                '}';
    }
}
