package com.poortoys.examples.initilizer;

import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.entities.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class UserInitializer implements Initializer {

    private UserDAO userDAO;

    public UserInitializer(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void initialize() {
        System.out.println("Initializing users...");

        // List of users with their attributes
        List<User> users = Arrays.asList(
            new User("john_doe", "john.doe@example.com", "hashedpassword1"),
            new User("jane_smith", "jane.smith@example.com", "hashedpassword2"),
            new User("alice_jones", "alice.jones@example.com", "hashedpassword3"),
            new User("bob_brown", "bob.brown@example.com", "hashedpassword4"),
            new User("charlie_davis", "charlie.davis@example.com", "hashedpassword5"),
            new User("david_wilson", "david.wilson@example.com", "hashedpassword6"),
            new User("emma_thomas", "emma.thomas@example.com", "hashedpassword7"),
            new User("olivia_martin", "olivia.martin@example.com", "hashedpassword8"),
            new User("liam_jackson", "liam.jackson@example.com", "hashedpassword9"),
            new User("sophia_white", "sophia.white@example.com", "hashedpassword10")
        );

        // Add each user if they don't already exist
        for (User user : users) {
            if (userDAO.findByEmail(user.getEmail()) == null) {
                userDAO.create(user);
                System.out.println("Added user: " + user.getUserName());
            } else {
                System.out.println("User already exists: " + user.getUserName());
            }
        }
        System.out.println("Users initialization completed.\n");
    }
}
