package com.poortoys.examples.initilizer;

import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.entities.Booking;
import com.poortoys.examples.entities.BookingStatus;
import com.poortoys.examples.entities.User;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BookingInitializer implements Initializer {

    private BookingDAO bookingDAO;
    private UserDAO userDAO;

    public BookingInitializer(BookingDAO bookingDAO, UserDAO userDAO) {
        this.bookingDAO = bookingDAO;
        this.userDAO = userDAO;
    }

    @Override
    public void initialize() {
        System.out.println("Initializing bookings...");

        // Example data for bookings
        List<BookingData> bookings = Arrays.asList(
                new BookingData("john_doe", "john.doe@example.com", "2024-06-15 12:00:00",
                        "2024-06-15 11:50:00", "2024-06-15 12:05:00",
                        new BigDecimal("300.00"), new BigDecimal("20.00"),
                        new BigDecimal("280.00"), BookingStatus.CONFIRMED),
                
                new BookingData("jane_smith", "jane.smith@example.com", "2024-07-20 14:00:00",
                        "2024-07-20 13:45:00", "2024-07-20 14:10:00",
                        new BigDecimal("75.00"), new BigDecimal("0.00"),
                        new BigDecimal("75.00"), BookingStatus.CONFIRMED)
        );

        for (BookingData data : bookings) {
            User user = userDAO.findByUsername(data.userName);

            if (user != null) {
            	Booking booking = new Booking(user, data.email, data.deliveryTime, data.timePaid, data.timeSent,
                        data.totalPrice, data.discount, data.finalPrice, data.bookingStatus);
                bookingDAO.create(booking);
                System.out.println("Added booking for user: " + data.userName);
            } else {
                System.out.println("User not found for booking: " + data.userName);
            }
        }
        System.out.println("Bookings initialization completed.\n");
    }

    // Helper class to structure booking details
    private static class BookingData {
        String userName;
        String email;
        Date deliveryTime;
        Date timePaid;
        Date timeSent;
        BigDecimal totalPrice;
        BigDecimal discount;
        BigDecimal finalPrice;
        BookingStatus bookingStatus;

        BookingData(String userName, String email, String deliveryTime, String timePaid,
                    String timeSent, BigDecimal totalPrice, BigDecimal discount,
                    BigDecimal finalPrice, BookingStatus bookingStatus) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                this.userName = userName;
                this.email = email;
                this.deliveryTime = formatter.parse(deliveryTime);
                this.timePaid = formatter.parse(timePaid);
                this.timeSent = formatter.parse(timeSent);
                this.totalPrice = totalPrice;
                this.discount = discount;
                this.finalPrice = finalPrice;
                this.bookingStatus = bookingStatus;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse dates for booking initialization", e);
            }
        }
    }
}
