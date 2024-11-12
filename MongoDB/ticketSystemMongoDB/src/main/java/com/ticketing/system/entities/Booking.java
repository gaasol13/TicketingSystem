package com.ticketing.system.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.Property;

@Entity("bookings") // Maps to the 'bookings' collection
@Indexes({
    @Index(fields = @Field("user_id"), options = @IndexOptions(name = "user_id_idx")), // Index on user_id
    @Index(fields = @Field("status"), options = @IndexOptions(name = "status_idx"))   // Index on status
})
public class Booking {
	
	@Id
	private ObjectId id;
	
	//ID if the user who made the booking 
	@Property("user_id")
	private ObjectId userId;
	
	 // **Add event_id to Booking**
    @Property("event_id")
    private ObjectId eventId;
	
	//Email address for ticket 
	@Property("delivery_email")
	private String deliveryEmail;
	
	@Property("delivery_time")
	private Date deliveryTime;
	
	@Property("time_paid")
	private Date timePaid;
	
    // Timestamp when tickets were sent
    @Property("time_sent")
    private Date timeSent;

    // Total price before discounts
    @Property("total_price")
    private BigDecimal totalPrice;

    // Discount applied to the booking
    @Property("discount")
    private BigDecimal discount;

    // Final price after discounts
    @Property("final_price")
    private BigDecimal finalPrice;

    // Status of the booking ('confirmed', 'in-progress', 'canceled')
    @Property("status")
    private String status;

    // List of ticket ObjectIds associated with the booking
    @Property("tickets")
    private List<ObjectId> tickets;

    // Default constructor
    public Booking() {}

	public Booking(ObjectId userId, ObjectId eventId, String deliveryEmail, Date deliveryTime, Date timePaid, Date timeSent,
			BigDecimal totalPrice, BigDecimal discount, BigDecimal finalPrice, String status, List<ObjectId> tickets) {
		super();
		this.userId = userId;
		  this.eventId = eventId;
		this.deliveryEmail = deliveryEmail;
		this.deliveryTime = deliveryTime;
		this.timePaid = timePaid;
		this.timeSent = timeSent;
		this.totalPrice = totalPrice;
		this.discount = discount;
		this.finalPrice = finalPrice;
		this.status = status;
		this.tickets = tickets;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public ObjectId getUserId() {
		return userId;
	}

	public void setUserId(ObjectId userId) {
		this.userId = userId;
	}
	  public ObjectId getEventId() {
	        return eventId;
	    }

	    public void setEventId(ObjectId eventId) {
	        this.eventId = eventId;
	    }

	public String getDeliveryEmail() {
		return deliveryEmail;
	}

	public void setDeliveryEmail(String deliveryEmail) {
		this.deliveryEmail = deliveryEmail;
	}

	public Date getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(Date deliveryTime) {
		this.deliveryTime = deliveryTime;
	}

	public Date getTimePaid() {
		return timePaid;
	}

	public void setTimePaid(Date timePaid) {
		this.timePaid = timePaid;
	}

	public Date getTimeSent() {
		return timeSent;
	}

	public void setTimeSent(Date timeSent) {
		this.timeSent = timeSent;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}

	public BigDecimal getFinalPrice() {
		return finalPrice;
	}

	public void setFinalPrice(BigDecimal finalPrice) {
		this.finalPrice = finalPrice;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<ObjectId> getTickets() {
		return tickets;
	}

	public void setTickets(List<ObjectId> tickets) {
		this.tickets = tickets;
	}

	@Override
	public String toString() {
		return "Booking [id=" + id + ", userId=" + userId + ", eventId=" + eventId + ", deliveryEmail=" + deliveryEmail + ", deliveryTime="
				+ deliveryTime + ", timePaid=" + timePaid + ", timeSent=" + timeSent + ", totalPrice=" + totalPrice
				+ ", discount=" + discount + ", finalPrice=" + finalPrice + ", status=" + status + ", tickets="
				+ tickets + "]";
	}
	
    

}
