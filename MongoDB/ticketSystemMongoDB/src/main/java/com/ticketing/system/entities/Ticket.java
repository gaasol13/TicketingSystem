package com.ticketing.system.entities;

import java.math.BigDecimal;
import java.util.Date;

import org.bson.types.Binary;
import org.bson.types.ObjectId;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.Property;

@Entity("tickets") // Maps to the 'tickets' collection
@Indexes({
	@Index(fields = @Field("serial_number"), options = @IndexOptions(unique = true)),
	@Index(fields = {@Field("event_id"), @Field("status")}),
	@Index(fields = @Field("ticket_category"))
})
public class Ticket {
	// Primary key (_id) of the ticket document
	@Id
	private ObjectId id;

	// Unique serial number to prevent duplication
	@Property("serial_number")
	private String serialNumber;

	// References the events collection
	@Property("event_id")
	private ObjectId eventId;

	// Denormalized ticket category description
	@Property("ticket_category")
	private String ticketCategory;

	// Section of the venue
	@Property("section")
	private String section;

	// Row number within the section
	@Property("row_number")
	private String rowNumber;

	// Seat number within the row
	@Property("seat_number")
	private String seatNumber;

	// Status of the ticket to track availability
	@Property("status")
	private String status; // "available", "reserved", "booked"

	// Timestamp of when the ticket was purchased
	@Property("purchase_date")
	private Date purchaseDate;
	 // New fields for pessimistic locking
    private Date lockTimestamp;
    private Binary lockSessionId;  // To store MongoDB session ID
    
    // ... existing getters and setters ...
    
    public Date getLockTimestamp() {
        return lockTimestamp;
    }
    
    public void setLockTimestamp(Date lockTimestamp) {
        this.lockTimestamp = lockTimestamp;
    }
    
    public Binary getLockSessionId() {
        return lockSessionId;
    }
    
    public void setLockSessionId(Binary lockSessionId) {
        this.lockSessionId = lockSessionId;
    }

	// Default constructor
	public Ticket() {}

	// Constructor with parameters
	public Ticket(String serialNumber, ObjectId eventId, String ticketCategory, String section,
			String rowNumber, String seatNumber, String status, Date purchaseDate) {
		this.serialNumber = serialNumber;
		this.eventId = eventId;
		this.ticketCategory = ticketCategory;
		this.section = section;
		this.rowNumber = rowNumber;
		this.seatNumber = seatNumber;
		this.status = status;
		this.purchaseDate = purchaseDate;
	}

	// Getters and setters

	public ObjectId getId() {
		return id;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public ObjectId getEventId() {
		return eventId;
	}

	public void setEventId(ObjectId eventId) {
		this.eventId = eventId;
	}

	public String getTicketCategory() {
		return ticketCategory;
	}

	public void setTicketCategory(String ticketCategory) {
		this.ticketCategory = ticketCategory;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(String rowNumber) {
		this.rowNumber = rowNumber;
	}

	public String getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(String seatNumber) {
		this.seatNumber = seatNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

    

	// Optional: Override toString() for better readability
	@Override
	public String toString() {
		return "Ticket{" +
				"id=" + id +
				", serialNumber='" + serialNumber + '\'' +
				", eventId=" + eventId +
				", ticketCategory='" + ticketCategory + '\'' +
				", section='" + section + '\'' +
				", rowNumber='" + rowNumber + '\'' +
				", seatNumber='" + seatNumber + '\'' +
				", status='" + status + '\'' +
				", purchaseDate=" + purchaseDate + 
				'}';
	}
}
