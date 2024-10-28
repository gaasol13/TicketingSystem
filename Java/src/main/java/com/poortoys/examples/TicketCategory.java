package com.poortoys.examples;

import javax.persistence.*;
import java.math.*;
import java.util.Date;

@Entity
@Table(name = "ticket_category")
public class TicketCategory {
	
	//Category of tickets for an event
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ticket_category_id")
	private int ticketCategoryId;
	
	
	//Description of the ticket category
	@Column(name = "description", nullable = false, length = 100)
	private String description;
	
	//Price of the ticket
	@Column(name ="price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;
	
	//Start date for the ticket category availability
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_date")
	private Date startDate;
	
	//Optional end date for the ticket category availability
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_date")
	private Date endDate;
	
	//Specific area or section within the venue
	@Column(name = "area", length = 30)
	private String area;
	
	//Many ticket categories can belong to one event
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id")
	private Event event;

	//Default constructor
	public TicketCategory() {
	}

	//Constructor 
	public TicketCategory(String description, BigDecimal price, Date startDate, Date endDate, String area,
			Event event) {
		super();
		this.description = description;
		this.price = price;
		this.startDate = startDate;
		this.endDate = endDate;
		this.area = area;
		this.event = event;
	}

	
	//Getter and setters
	public int getTicketCategoryId() {
		return ticketCategoryId;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	@Override
	public String toString() {
		return "TicketCategory [ticketCategoryId=" + ticketCategoryId 
				+ ", description=" + description 
				+ ", price=" + price
				+ ", startDate=" + startDate 
				+ ", endDate=" + endDate 
				+ ", area=" + area 
				+ ", event=" + (event != null ? event.getEventName() : "null")
				+ "]";
	}

	
	

}
