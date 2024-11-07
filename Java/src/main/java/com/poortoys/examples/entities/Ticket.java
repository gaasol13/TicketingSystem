package com.poortoys.examples.entities;

import javax.persistence.*;

import java.math.BigDecimal;
import java.util.Date;

//Represents an individual ticket for an event

@Entity
@Table(name = "tickets")
public class Ticket {
	
	//Primary key of the tickets table, auto-generated
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ticket_id")
	private int ticketId;
	
	//Unique serial number to prevent duplication
	@Column(name = "serial_number", unique = true, nullable = false, length = 255)
	private String serialNumber;
	
	//Many tickets belong to one event
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id")
	private Event event;
	
	//Many tickets belong to one ticket category
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticket_category_id")
	private TicketCategory ticketCategory;
	
	//Section of the venue, cannot be null, maximum length of 30 characters
	@Column(name = "section", nullable = false, length = 30)
	private String section;
	
	//Row number within the section
	@Column(name = "rownumber", nullable = false, length = 10)
	private String rowNumber;
	
	//Seat number within the row
	@Column(name = "seat_number", nullable = false, length = 10)
	private String seatNumber;
	
	//Status of the ticket to track availability, defaults to "AVAILABLE"
	//@Enumerated(EnumType.STRING)
	@Convert(converter = TicketStatusConverter.class)
	@Column(name = "status", nullable = false, length = 10)
	private TicketStatus status = TicketStatus.AVAILABLE;
	
	//Timestamp of when the ticket was purchased
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "purchase_date")
	private Date purchaseDate;
	
	//Default constructor
	public Ticket() {
		// TODO Auto-generated constructor stub
	}

	public Ticket(String serialNumber, Event event, TicketCategory ticketCategory, String section, String rowNumber,
			String seatNumber, TicketStatus status) {
		this.serialNumber = serialNumber;
		this.event = event;
		this.ticketCategory = ticketCategory;
		this.section = section;
		this.rowNumber = rowNumber;
		this.seatNumber = seatNumber;
		this.status = status;
	}

	//Setters and getters
	public int getTicketId() {
		return ticketId;
	}
	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public TicketCategory getTicketCategory() {
		return ticketCategory;
	}

	public void setTicketCategory(TicketCategory ticketCategory) {
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

	public TicketStatus getStatus() {
		return status;
	}

	public void setStatus(TicketStatus status) {
		this.status = status;
	}

	public Date getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	@Override
	public String toString() {
		return "Ticket [ticketId=" + ticketId + 
				", serialNumber=" + serialNumber + 
				", event=" + event
				+ ", ticketCategory=" + (ticketCategory != null ? ticketCategory.getDescription() : "null")
				+ ", section=" + section 
				+ ", rowNumber=" + rowNumber
				+ ", seatNumber=" + seatNumber 
				+ ", status=" + status 
				+ ", purchaseDate=" + purchaseDate + "]";
	}
	
	//Override
	

	
	

}
