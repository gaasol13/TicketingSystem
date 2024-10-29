package com.poortoys.examples.initilizer;


import com.poortoys.examples.dao.*;
import com.poortoys.examples.entities.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * The DataInitializer class is responsible for populating the database with sample data.
 * It creates and persists entities such as Genres, Performers, Venues, Events,
 * TicketCategories, Tickets, Users, Bookings, and BookingTickets.
 */
public class DataInitializer {

    // The name of the persistence unit defined in persistence.xml
    private static final String PERSISTENCE_UNIT_NAME = "ticketingsystem";

    // EntityManagerFactory and EntityManager for interacting with the persistence context
    private EntityManagerFactory emf;
    private EntityManager em;

    // DAO instances for each entity
    private GenreDAO genreDAO;
    private PerformerDAO performerDAO;
    private VenueDAO venueDAO;
    private EventDAO eventDAO;
    private TicketCategoryDAO ticketCategoryDAO;
    private TicketDAO ticketDAO;
    private UserDAO userDAO;
    private BookingDAO bookingDAO;
    private BookingTicketDAO bookingTicketDAO;

    /**
     * Constructor that initializes the EntityManagerFactory, EntityManager, and DAOs.
     */
    public DataInitializer() {
        // Create an EntityManagerFactory based on the persistence unit
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        // Create an EntityManager to manage entities
        em = emf.createEntityManager();

        // Initialize DAOs with the EntityManager
        genreDAO = new GenreDAO(em);
        performerDAO = new PerformerDAO(em);
        venueDAO = new VenueDAO(em);
        eventDAO = new EventDAO(em);
        ticketCategoryDAO = new TicketCategoryDAO(em);
        ticketDAO = new TicketDAO(em);
        userDAO = new UserDAO(em);
        bookingDAO = new BookingDAO(em);
        bookingTicketDAO = new BookingTicketDAO(em);
    }

    /**
     * The main method serves as the entry point of the application.
     * It creates an instance of DataInitializer, calls the populateData method,
     * and then closes the EntityManager and EntityManagerFactory.
     *
     * Note: It's better practice to have the main method in a separate AppMain class.
     * See the explanation below.
     */
    public static void main(String[] args) {
        DataInitializer initializer = new DataInitializer();
        initializer.populateData();
        initializer.close();
    }

    /**
     * Populates the database with sample data within a transaction.
     * It calls methods to create and persist each entity type.
     * This method ensures that all data is inserted atomically.
     */
    public void populateData() {
        try {
            // Begin a new transaction
            em.getTransaction().begin();

            // Call methods to create and persist entities
            createGenres();
            createPerformers();
            createVenues();
            createEvents();
            createTicketCategories();
            createTickets();
            createUsers();
            createBookings();

            // Commit the transaction after successful operations
            em.getTransaction().commit();

            // Optional: Validate data insertion by counting records
            validateData();

        } catch (Exception e) {
            // Roll back the transaction in case of any errors
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // Print stack trace for debugging
            e.printStackTrace();
        }
    }

    /**
     * Closes the EntityManager and EntityManagerFactory to release resources.
     */
    public void close() {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }

    /**
     * Creates and persists Genre entities.
     * Genres are the different types of music or performance categories.
     * This method avoids duplication by checking if a genre already exists before inserting.
     */
    private void createGenres() {
        System.out.println("Creating Genres...");

        // List of genres to be created
        List<String> genreNames = Arrays.asList(
                "Rock", "Jazz", "Classical", "Pop",
                "Electronic", "Hip-Hop", "Country",
                "Blues", "Reggae", "Metal"
        );

        // Iterate over each genre name
        for (String name : genreNames) {
            // Check if the genre already exists using GenreDAO
            Genre existingGenre = genreDAO.findByName(name);
            if (existingGenre == null) {
                // If not exists, create and persist the new Genre
                Genre genre = new Genre(name);
                genreDAO.create(genre);
                System.out.println("Added Genre: " + name);
            } else {
                // If exists, skip to avoid duplication
                System.out.println("Genre already exists: " + name);
            }
        }

        System.out.println("Genres created.");
    }

    /**
     * Creates and persists Performer entities.
     * Each Performer is associated with a Genre.
     * This method avoids duplication by checking if a performer already exists before inserting.
     */
    private void createPerformers() {
        System.out.println("Creating Performers...");

        // List of performers to be created
        List<String> performerNames = Arrays.asList(
                "The Rockers", "Jazz Masters", "Classical Quartet", "Pop Icons",
                "Electronica", "Hip-Hop Crew", "Country Stars", "Blues Band",
                "Reggae Rhythms", "Metal Heads"
        );

        // Iterate over each performer name
        for (String name : performerNames) {
            // Check if the performer already exists using PerformerDAO
            Performer existingPerformer = performerDAO.findByName(name);
            if (existingPerformer == null) {
                // Determine the genre associated with the performer
                String genreName = determineGenreFromPerformer(name);
                Genre genre = genreDAO.findByName(genreName);

                if (genre != null) {
                    // Create and persist the new Performer
                    Performer performer = new Performer(name, genre);
                    performerDAO.create(performer);
                    System.out.println("Added Performer: " + name);
                } else {
                    System.out.println("Genre not found for Performer: " + name);
                }
            } else {
                // If exists, skip to avoid duplication
                System.out.println("Performer already exists: " + name);
            }
        }

        System.out.println("Performers created.");
    }

    /**
     * Determines the genre based on the performer's name.
     *
     * @param performerName Name of the performer
     * @return Associated genre name
     */
    private String determineGenreFromPerformer(String performerName) {
        if (performerName.toLowerCase().contains("rock")) {
            return "Rock";
        } else if (performerName.toLowerCase().contains("jazz")) {
            return "Jazz";
        } else if (performerName.toLowerCase().contains("classical")) {
            return "Classical";
        } else if (performerName.toLowerCase().contains("pop")) {
            return "Pop";
        } else if (performerName.toLowerCase().contains("electronica")) {
            return "Electronic";
        } else if (performerName.toLowerCase().contains("hip-hop")) {
            return "Hip-Hop";
        } else if (performerName.toLowerCase().contains("country")) {
            return "Country";
        } else if (performerName.toLowerCase().contains("blues")) {
            return "Blues";
        } else if (performerName.toLowerCase().contains("reggae")) {
            return "Reggae";
        } else if (performerName.toLowerCase().contains("metal")) {
            return "Metal";
        } else {
            return "Unknown";
        }
    }

    /**
     * Creates and persists Venue entities.
     * Venues are locations where events take place.
     * This method avoids duplication by checking if a venue already exists before inserting.
     */
    private void createVenues() {
        System.out.println("Creating Venues...");

        // List of venues to be created
        List<Venue> venues = Arrays.asList(
                new Venue("Grand Arena", "123 Main St, Anytown", "Stadium", 50000),
                new Venue("Jazz Club", "456 Oak Ave, Music City", "Club", 200),
                new Venue("Symphony Hall", "789 Pine Rd, Harmony", "Concert Hall", 1500),
                new Venue("Pop Dome", "321 Maple St, Popville", "Arena", 30000),
                new Venue("Electronica Center", "654 Elm St, Tech City", "Exhibition Center", 10000),
                new Venue("Hip-Hop Venue", "987 Cedar Blvd, Beat Town", "Club", 800),
                new Venue("Country Grounds", "543 Birch Ln, Nashville", "Open Air", 25000),
                new Venue("Blues House", "210 Willow Dr, Bluesville", "Bar", 150),
                new Venue("Reggae Beach", "369 Ocean Ave, Island City", "Beach", 5000),
                new Venue("Metal Hall", "852 Steel Rd, Metal City", "Hall", 7000)
        );

        // Iterate over each venue
        for (Venue venue : venues) {
            // Check if the venue already exists using VenueDAO
            Venue existingVenue = venueDAO.findByName(venue.getVenueName());
            if (existingVenue == null) {
                // Persist the new Venue
                venueDAO.create(venue);
                System.out.println("Added Venue: " + venue.getVenueName());
            } else {
                // If exists, skip to avoid duplication
                System.out.println("Venue already exists: " + venue.getVenueName());
            }
        }

        System.out.println("Venues created.");
    }

    /**
     * Creates and persists Event entities.
     * Each Event links a Performer to a Venue on a specific date.
     * This method avoids duplication by checking if an event already exists before inserting.
     */
    private void createEvents() {
        System.out.println("Creating Events...");

        // List of events to be created with their details
        List<EventData> eventDataList = Arrays.asList(
                new EventData("Rock Fest 2024", "The Rockers", LocalDate.of(2024, 6, 15), "Grand Arena"),
                new EventData("Jazz Nights", "Jazz Masters", LocalDate.of(2024, 7, 20), "Jazz Club"),
                new EventData("Classical Evening", "Classical Quartet", LocalDate.of(2024, 8, 5), "Symphony Hall"),
                new EventData("Pop Extravaganza", "Pop Icons", LocalDate.of(2024, 9, 10), "Pop Dome"),
                new EventData("Electronic Beats", "Electronica", LocalDate.of(2024, 10, 25), "Electronica Center"),
                new EventData("Hip-Hop Bash", "Hip-Hop Crew", LocalDate.of(2024, 11, 15), "Hip-Hop Venue"),
                new EventData("Country Fair", "Country Stars", LocalDate.of(2024, 12, 5), "Country Grounds"),
                new EventData("Blues Festival", "Blues Band", LocalDate.of(2025, 1, 20), "Blues House"),
                new EventData("Reggae Summer", "Reggae Rhythms", LocalDate.of(2025, 2, 25), "Reggae Beach"),
                new EventData("Metal Mania", "Metal Heads", LocalDate.of(2025, 3, 30), "Metal Hall")
        );

        // Iterate over each event data
        for (EventData data : eventDataList) {
            // Check if the event already exists using EventDAO
            Event existingEvent = eventDAO.findByName(data.getEventName());
            if (existingEvent == null) {
                // Find the Performer by name
                Performer performer = performerDAO.findByName(data.getPerformerName());
                if (performer == null) {
                    System.out.println("Performer not found: " + data.getPerformerName());
                    continue; // Skip if performer not found
                }

                // Find the Venue by name
                Venue venue = venueDAO.findByName(data.getVenueName());
                if (venue == null) {
                    System.out.println("Venue not found: " + data.getVenueName());
                    continue; // Skip if venue not found
                }

                // Create and persist the new Event
                Event event = new Event(data.getEventName(), performer, data.getEventDate(), venue);
                eventDAO.create(event);
                System.out.println("Added Event: " + data.getEventName());
            } else {
                // If exists, skip to avoid duplication
                System.out.println("Event already exists: " + data.getEventName());
            }
        }

        System.out.println("Events created.");
    }

    /**
     * Creates and persists TicketCategory entities.
     * TicketCategories define pricing and availability periods for tickets.
     * This method avoids duplication by checking if a TicketCategory already exists before inserting.
     */
    private void createTicketCategories() {
        System.out.println("Creating Ticket Categories...");

        // Map to hold ticket category data for each event
        Map<String, List<TicketCategoryData>> ticketCategoriesMap = new HashMap<>();

        // Define TicketCategories for specific events
        ticketCategoriesMap.put("Rock Fest 2024", Arrays.asList(
                new TicketCategoryData("VIP", new BigDecimal("150.00"),
                        LocalDateTime.of(2024, 5, 1, 0, 0),
                        LocalDateTime.of(2024, 6, 14, 23, 59),
                        "Front Row"),
                new TicketCategoryData("General Admission", new BigDecimal("75.00"),
                        LocalDateTime.of(2024, 5, 1, 0, 0),
                        LocalDateTime.of(2024, 6, 14, 23, 59),
                        "Middle Section"),
                new TicketCategoryData("Balcony", new BigDecimal("50.00"),
                        LocalDateTime.of(2024, 5, 1, 0, 0),
                        LocalDateTime.of(2024, 6, 14, 23, 59),
                        "Upper Balcony")
        ));

        ticketCategoriesMap.put("Jazz Nights", Arrays.asList(
                new TicketCategoryData("VIP", new BigDecimal("120.00"),
                        LocalDateTime.of(2024, 6, 1, 0, 0),
                        LocalDateTime.of(2024, 7, 19, 23, 59),
                        "Front Stage"),
                new TicketCategoryData("General Admission", new BigDecimal("60.00"),
                        LocalDateTime.of(2024, 6, 1, 0, 0),
                        LocalDateTime.of(2024, 7, 19, 23, 59),
                        "Main Floor")
        ));

        // Add more TicketCategories for other events as needed

        // Iterate over each event to create its TicketCategories
        for (Map.Entry<String, List<TicketCategoryData>> entry : ticketCategoriesMap.entrySet()) {
            String eventName = entry.getKey();
            List<TicketCategoryData> categoriesData = entry.getValue();

            // Find the Event by name
            Event event = eventDAO.findByName(eventName);
            if (event == null) {
                System.out.println("Event not found for TicketCategories: " + eventName);
                continue; // Skip if event not found
            }

            // Iterate over each TicketCategoryData to create TicketCategories
            for (TicketCategoryData data : categoriesData) {
                // Check if the TicketCategory already exists using TicketCategoryDAO
                TicketCategory existingCategory = ticketCategoryDAO.findByTypeAndEvent(data.getType(), event);
                if (existingCategory == null) {
                    // Create and persist the new TicketCategory
                    TicketCategory category = new TicketCategory(
                            data.getType(),
                            data.getPrice(),
                            data.getAvailableFrom(),
                            data.getAvailableTo(),
                            data.getArea(),
                            event
                    );
                    ticketCategoryDAO.create(category);
                    System.out.println("Added TicketCategory: " + data.getType() + " for Event: " + eventName);
                } else {
                    // If exists, skip to avoid duplication
                    System.out.println("TicketCategory already exists: " + data.getType() + " for Event: " + eventName);
                }
            }
        }

        System.out.println("Ticket Categories created.");
    }

    /**
     * Creates and persists Ticket entities.
     * Tickets represent individual seats available for purchase.
     * This method avoids duplication by checking if a ticket already exists before inserting.
     */
    private void createTickets() {
        System.out.println("Creating Tickets...");

        // Retrieve all events to associate tickets
        List<Event> events = eventDAO.findAll();

        // Iterate over each event to create Tickets
        for (Event event : events) {
            // Retrieve all TicketCategories for the current event
            List<TicketCategory> categories = ticketCategoryDAO.findByEvent(event);

            for (TicketCategory category : categories) {
                int numberOfTickets = 100; // Number of tickets per category

                // Create Tickets for each category
                for (int i = 1; i <= numberOfTickets; i++) {
                    // Generate a unique serial number for the ticket
                    String serialNumber = generateSerialNumber(event.getEventName(), category.getType(), i);
                    // Determine the section based on the TicketCategory area
                    String section = determineSection(category.getArea());
                    // Determine row number and seat number
                    String rowNumber = String.valueOf((i - 1) / 10 + 1); // Rows 1-10
                    String seatNumber = String.valueOf(i % 10 == 0 ? 10 : i % 10); // Seats 1-10

                    // Check if the ticket already exists using TicketDAO
                    Ticket existingTicket = ticketDAO.findBySerialNumber(serialNumber);
                    if (existingTicket == null) {
                        // Create and persist the new Ticket
                        Ticket ticket = new Ticket(
                                serialNumber,
                                event,
                                category,
                                section,
                                rowNumber,
                                seatNumber,
                                "available",
                                null // Purchase date is null for available tickets
                        );
                        ticketDAO.create(ticket);
                    }
                }
                System.out.println("Tickets created for TicketCategory: " + category.getType() + " in Event: " + event.getEventName());
            }
        }

        System.out.println("Tickets created.");
    }

    /**
     * Creates and persists User entities.
     * Users represent customers who can book tickets.
     * This method avoids duplication by checking if a user already exists before inserting.
     */
    private void createUsers() {
        System.out.println("Creating Users...");

        // List of users to be created
        List<User> users = Arrays.asList(
                new User("john_doe", "john.doe@example.com", hashPassword("password1"), "CONF123", LocalDateTime.of(2024, 1, 15, 10, 0)),
                new User("jane_smith", "jane.smith@example.com", hashPassword("password2"), "CONF124", LocalDateTime.of(2024, 2, 20, 11, 30)),
                new User("alice_jones", "alice.jones@example.com", hashPassword("password3"), "CONF125", LocalDateTime.of(2024, 3, 25, 9, 45)),
                // Add more users as needed
                new User("bob_brown", "bob.brown@example.com", hashPassword("password4"), "CONF126", LocalDateTime.of(2024, 4, 10, 14, 20))
        );

        // Iterate over each user
        for (User user : users) {
            // Check if the user already exists using UserDAO
            User existingUser = userDAO.findByUsername(user.getUserName());
            if (existingUser == null) {
                // Persist the new User
                userDAO.create(user);
                System.out.println("Added User: " + user.getUserName());
            } else {
                // If exists, skip to avoid duplication
                System.out.println("User already exists: " + user.getUserName());
            }
        }

        System.out.println("Users created.");
    }

    /**
     * Creates and persists Booking entities along with associated BookingTicket entities.
     * Bookings represent a user's purchase of tickets.
     * This method avoids duplication by ensuring bookings are unique per user and tickets are available.
     */
    private void createBookings() {
        System.out.println("Creating Bookings...");

        // Map of bookings: username to ticket serial numbers
        Map<String, String> bookingMap = new LinkedHashMap<>();
        bookingMap.put("john_doe", "RFVI0001,RFVI0002");
        bookingMap.put("jane_smith", "RFGA0001");
        bookingMap.put("alice_jones", "JNVIP0001,JNVIP0002");
        bookingMap.put("bob_brown", "JNGA0001");

        // Iterate over each booking entry
        for (Map.Entry<String, String> entry : bookingMap.entrySet()) {
            String username = entry.getKey();
            String[] ticketSerials = entry.getValue().split(",");

            // Find User by username
            User user = userDAO.findByUsername(username);
            if (user == null) {
                System.out.println("User not found: " + username);
                continue; // Skip if user not found
            }

            // Initialize pricing variables
            BigDecimal totalPrice = BigDecimal.ZERO;
            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal finalPrice = BigDecimal.ZERO;

            List<Ticket> ticketsToBook = new ArrayList<>();

            // Retrieve and validate tickets
            for (String serial : ticketSerials) {
                Ticket ticket = ticketDAO.findBySerialNumber(serial.trim());
                if (ticket == null) {
                    System.out.println("Ticket not found: " + serial);
                    continue; // Skip if ticket not found
                }

                if (!"available".equalsIgnoreCase(ticket.getStatus())) {
                    System.out.println("Ticket not available: " + serial);
                    continue; // Skip if ticket is not available
                }

                // Add ticket to the list for booking
                ticketsToBook.add(ticket);

                // Accumulate total price
                totalPrice = totalPrice.add(ticket.getTicketCategory().getPrice());
            }

            // Apply discounts based on custom logic (e.g., fixed discount)
            if ("john_doe".equalsIgnoreCase(username)) {
                discount = new BigDecimal("20.00");
            }

            // Calculate final price
            finalPrice = totalPrice.subtract(discount);

            // Create and persist Booking entity
            Booking booking = new Booking(
                    user,
                    user.getEmail(),
                    LocalDateTime.now(), // delivery_time
                    LocalDateTime.now().minusMinutes(10), // time_paid
                    LocalDateTime.now(), // time_sent
                    totalPrice,
                    discount,
                    finalPrice,
                    "confirmed" // booking_status
            );
            bookingDAO.create(booking);
            System.out.println("Created Booking for user: " + username);

            // Update tickets and create BookingTicket entities
            for (Ticket ticket : ticketsToBook) {
                // Update ticket status to 'booked' and set purchase date
                ticket.setStatus("booked");
                ticket.setPurchaseDate(LocalDateTime.now());
                ticketDAO.update(ticket);

                // Create and persist BookingTicket entity
                BookingTicket bookingTicket = new BookingTicket(booking, ticket);
                bookingTicketDAO.create(bookingTicket);
            }
        }

        System.out.println("Bookings created.");
    }

    /**
     * Validates the data insertion by querying and printing counts of records.
     */
    private void validateData() {
        System.out.println("Validating Data...");

        // Count and print the number of Genres
        Long genreCount = genreDAO.count();
        System.out.println("Total Genres: " + genreCount);

        // Count and print the number of Performers
        Long performerCount = performerDAO.count();
        System.out.println("Total Performers: " + performerCount);

        // Count and print the number of Venues
        Long venueCount = venueDAO.count();
        System.out.println("Total Venues: " + venueCount);

        // Continue for other entities as needed
    }

    /**
     * Generates a unique serial number for a ticket based on event name, category, and seat number.
     *
     * @param eventName   Name of the event
     * @param category    Type of TicketCategory
     * @param seatNumber  Seat number
     * @return Generated serial number
     */
    private String generateSerialNumber(String eventName, String category, int seatNumber) {
        // Extract first two letters from event name (e.g., "RF" for "Rock Fest")
        String eventPrefix = eventName.split(" ")[0].substring(0, 2).toUpperCase();
        // Extract first two letters from category type (e.g., "VI" for "VIP")
        String categoryPrefix = category.substring(0, 2).toUpperCase();
        // Combine prefixes and seat number with leading zeros
        return eventPrefix + categoryPrefix + String.format("%04d", seatNumber);
    }

    /**
     * Determines the section of the venue based on the ticket category area.
     *
     * @param area Description of the area from TicketCategory
     * @return Section code
     */
    private String determineSection(String area) {
        switch (area.toLowerCase()) {
            case "front row":
            case "front stage":
                return "A";
            case "middle section":
            case "main floor":
                return "B";
            case "upper balcony":
            case "orchestra":
            case "mezzanine":
            case "golden circle":
            case "general area":
                return "C";
            default:
                return "Unknown";
        }
    }

    /**
     * Inner class to hold event data for creating Event entities.
     */
    private static class EventData {
        private String eventName;
        private String performerName;
        private LocalDate eventDate;
        private String venueName;

        public EventData(String eventName, String performerName, LocalDate eventDate, String venueName) {
            this.eventName = eventName;
            this.performerName = performerName;
            this.eventDate = eventDate;
            this.venueName = venueName;
        }

        public String getEventName() {
            return eventName;
        }

        public String getPerformerName() {
            return performerName;
        }

        public LocalDate getEventDate() {
            return eventDate;
        }

        public String getVenueName() {
            return venueName;
        }
    }

    /**
     * Inner class to hold ticket category data for creating TicketCategory entities.
     */
    private static class TicketCategoryData {
        private String type;
        private BigDecimal price;
        private LocalDateTime availableFrom;
        private LocalDateTime availableTo;
        private String area;

        public TicketCategoryData(String type, BigDecimal price, LocalDateTime availableFrom, LocalDateTime availableTo, String area) {
            this.type = type;
            this.price = price;
            this.availableFrom = availableFrom;
            this.availableTo = availableTo;
            this.area = area;
        }

        public String getType() {
            return type;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public LocalDateTime getAvailableFrom() {
            return availableFrom;
        }

        public LocalDateTime getAvailableTo() {
            return availableTo;
        }

        public String getArea() {
            return area;
        }
    }
}
