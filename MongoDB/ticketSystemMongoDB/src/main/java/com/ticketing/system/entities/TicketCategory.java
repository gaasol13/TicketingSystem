package com.ticketing.system.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import org.bson.types.ObjectId;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;

@Entity("ticketCategories") // Marks as embedded without separate collection
public class TicketCategory {
	//@Id
	//private Object id;

    @Property("description")
    private String description; // Description of the ticket category

    @Property("price")
    private BigDecimal price; // Price of the ticket

    @Property("start_date")
    private Date startDate; // Start date for ticket availability

    @Property("end_date")
    private Date endDate; // End date for ticket availability

    @Property("area")
    private String area; // Seating area description

    /**
     * Default constructor required by Morphia.
     */
    public TicketCategory() {
    }

    /**
     * Constructor to initialize all fields of TicketCategory.
     */
    public TicketCategory(String description, BigDecimal price, Date startDate, Date endDate, String area) {
        this.description = description;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.area = area;
    }

    // Getters and Setters

    // Getters and Setters
	/*
	 * public Object getId() { return id; } public void setId(ObjectId id) { this.id
	 * = id; }
	 */
    
    

// Getters and Setters

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


    /**
     * Overrides the default toString() method for better readability.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TicketCategory that = (TicketCategory) o;

        return Objects.equals(description, that.description) &&
               Objects.equals(area, that.area);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(description, area);
    }
    
    @Override
    public String toString() {
        return "TicketCategory{" +
                "description='" + description + '\'' +
                ", price=" + price +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", area='" + area + '\'' +
                '}';
    }
}