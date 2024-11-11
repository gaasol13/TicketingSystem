package com.ticketing.system.entities;

import java.util.Date;

import org.bson.types.ObjectId;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Property;

@Entity("users")
public class User {
	
	@Id
	private ObjectId id;
	
	//unique username for login
	@Property("user_name") //maps to username
	@Indexed(options = @dev.morphia.annotations.IndexOptions(unique = true, name = "userName_idx"))
	private String userName;
	
	//unique email address
	@Property("email")
	@Indexed(options = @dev.morphia.annotations.IndexOptions(unique = true, name = "email_idx"))
	private String email;
	
	@Property("password_hash")
	private String passwordHash;
	
	@Property("confirmation_code")
	private String confirmationCode;
	
	@Property("registration_date")
	private Date registrationDate;
	
	//default constructor
	public User() {
		
	}

    public User(String userName, String email, String passwordHash, String confirmationCode, Date registrationDate) {
        this.userName = userName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.confirmationCode = confirmationCode;
        this.registrationDate = registrationDate;
    }
    
    // Getter for id (no setter since it's auto-generated)
    public ObjectId getId() {
        return id;
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

    // Getter and setter for registrationDate
    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    // Optional: Override toString() for better readability
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", confirmationCode='" + confirmationCode + '\'' +
                ", registrationDate=" + registrationDate +
                '}';
    }
    
    
}
