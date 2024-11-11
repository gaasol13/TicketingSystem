package com.poortoys.examples.initializer;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.poortoys.examples.dao.UserDAO;
import com.ticketing.system.entities.User;

public class UserInitializer implements Initializer{
	
	private final UserDAO userDAO;
	
	public UserInitializer(UserDAO userDAO) {
		this.userDAO = userDAO;
	}
	
	// initializes the users collection by adding sample users, and avoid duplicate emails
	@Override
	public void initialize() {
		System.out.println("Initializing users...");
			
			// Date formatter for registration dates
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

	        // List of users with their attributes
	        List<String[]> usersData = Arrays.asList(
	            new String[]{"john_doe", "john.doe@example.com", "hashedpassword1", "CONF123", "2024-01-15T10:00:00Z"},
	            new String[]{"jane_smith", "jane.smith@example.com", "hashedpassword2", "CONF124", "2024-02-20T11:30:00Z"},
	            new String[]{"alice_jones", "alice.jones@example.com", "hashedpassword3", "CONF125", "2024-03-25T09:45:00Z"},
	            new String[]{"bob_brown", "bob.brown@example.com", "hashedpassword4", "CONF126", "2024-04-10T14:20:00Z"},
	            new String[]{"charlie_davis", "charlie.davis@example.com", "hashedpassword5", "CONF127", "2024-05-05T16:50:00Z"},
	            new String[]{"david_wilson", "david.wilson@example.com", "hashedpassword6", "CONF128", "2024-06-10T12:15:00Z"},
	            new String[]{"emma_thomas", "emma.thomas@example.com", "hashedpassword7", "CONF129", "2024-07-22T13:40:00Z"},
	            new String[]{"olivia_martin", "olivia.martin@example.com", "hashedpassword8", "CONF130", "2024-08-18T15:55:00Z"},
	            new String[]{"liam_jackson", "liam.jackson@example.com", "hashedpassword9", "CONF131", "2024-09-12T17:05:00Z"},
	            new String[]{"sophia_white", "sophia.white@example.com", "hashedpassword10", "CONF132", "2024-10-05T18:25:00Z"}
	        );
		
	        //iterate and add users
	        for (String[] userData : usersData) {
	        	String userName = userData[0];
	        	String email = userData[1];
	        	String passwordHash = userData[2];
	        	String confirmationCode = userData[3];
	        	String registrationDateStr = userData[4];
	        	
	        	try {
	        		//Parse registration date
	        		Date registrationDate = dateFormat.parse(registrationDateStr);
	        		
	        		//check if the email or username already exists
	        		boolean emailExists = userDAO.existsByEmail(email);
	        		boolean userNameExists = userDAO.existsByUserName(userName);
	        		
	        		if(emailExists || userNameExists) {
	        			System.out.println("User already exists with " + 
	        		(emailExists ? "email: " + email : "") + 
	        		(emailExists && userNameExists ? " and " : "") + 
	        		(userNameExists ? "username: " + userName : ""));
	        		}else {
	        			//create user object
	        			User user = new User(userName, email, passwordHash, confirmationCode, registrationDate);
	        			userDAO.create(user);
	        			System.out.println("Added user: " + userName + " with email " + email);
	        		}
	        	}catch (Exception e) {
	        		System.out.println("eeor parsing date for user: " + userName);
	        		e.printStackTrace();
	        	}
	        }
	        System.out.println("User intialization completed. \n");
	}
	
	

}
