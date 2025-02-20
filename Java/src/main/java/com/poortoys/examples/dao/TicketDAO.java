package com.poortoys.examples.dao;

import com.poortoys.examples.entities.Ticket;
import com.poortoys.examples.entities.TicketStatus;
import com.poortoys.examples.entities.User;

//import dev.morphia.query.experimental.filters.Filters;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

//import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketDAO {

    private EntityManager em;

    public TicketDAO(EntityManager em) {
        this.em = em;
    }

    // Find all tickets for a specific event
    public List<Ticket> findByEventId(int eventId) {
        TypedQuery<Ticket> query = em.createQuery("SELECT t FROM Ticket t WHERE t.event.eventId = :eventId", Ticket.class);
        query.setParameter("eventId", eventId);
        return query.getResultList();
    }

    // Find all available tickets for a specific event
    public List<Ticket> findAvailableTicketsByEventId(int eventId) {
        TypedQuery<Ticket> query = em.createQuery("SELECT t FROM Ticket t WHERE t.event.eventId = :eventId AND t.status = :status", Ticket.class);
        query.setParameter("eventId", eventId);
        query.setParameter("status", TicketStatus.AVAILABLE);
        return query.getResultList();
    }
    
    //finds and locks a ticket by its serial number using PESSIMISTIC_WRITE lock
    public Ticket findBySerialNumber(String serialNumber) {
        TypedQuery<Ticket> query = em.createQuery(
            "SELECT t FROM Ticket t WHERE t.serialNumber = :serialNumber", Ticket.class);
        query.setParameter("serialNumber", serialNumber);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        List<Ticket> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    
    public List<Ticket> findAll() {
        TypedQuery<Ticket> query = em.createQuery("SELECT u FROM Ticket u", Ticket.class);
        return query.getResultList();
    }

    // Persist a new ticket into the database
    public void create(Ticket ticket) {
        em.persist(ticket);
    }

    // Update an existing ticket in the database
    public Ticket update(Ticket ticket) {
        return em.merge(ticket);
    }

    // Delete a ticket from the database
    public void delete(Ticket ticket) {
        em.remove(em.contains(ticket) ? ticket : em.merge(ticket));
    }

    // Count the total number of tickets
    public Long count() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(t) FROM Ticket t", Long.class);
        return query.getSingleResult();
    }
    
    public List<Ticket> findAvailableTicketsWithLock(Integer eventId, TicketDAO status, int limit) {
        TypedQuery<Ticket> query = em.createQuery(
            "SELECT t FROM Ticket t " +
            "WHERE t.event.eventId = :eventId " +
            "AND t.status = :status " +
            "ORDER BY t.ticketId",
            Ticket.class
        );
        
        query.setParameter("eventId", eventId);
        query.setParameter("status", status);
        query.setMaxResults(limit);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        
        return query.getResultList();
    }
    
    
    

    public Map<String, Integer> getTotalTicketsByCategory(int eventId) {
        Map<String, Integer> totalTicketsByCategory = new HashMap<>();
        TypedQuery<Object[]> query = em.createQuery(
            "SELECT t.ticketCategory.categoryName, COUNT(t) FROM Ticket t WHERE t.event.eventId = :eventId GROUP BY t.ticketCategory.categoryName",
            Object[].class);
        query.setParameter("eventId", eventId);
        List<Object[]> results = query.getResultList();
        for (Object[] result : results) {
            String categoryName = (String) result[0];
            Long count = (Long) result[1];
            totalTicketsByCategory.put(categoryName, count.intValue());
        }
        return totalTicketsByCategory;
    }

    
    
    
    
    public Map<String, Integer> getTicketsBookedByCategory(int eventId) {
        Map<String, Integer> ticketsBookedByCategory = new HashMap<>();
        TypedQuery<Object[]> query = em.createQuery(
            "SELECT t.ticketCategory.categoryName, COUNT(t) FROM Ticket t WHERE t.event.eventId = :eventId AND t.status = :status GROUP BY t.ticketCategory.categoryName",
            Object[].class);
        query.setParameter("eventId", eventId);
        query.setParameter("status", TicketStatus.SOLD); // Adjust as per your TicketStatus enum
        List<Object[]> results = query.getResultList();
        for (Object[] result : results) {
            String categoryName = (String) result[0];
            Long count = (Long) result[1];
            ticketsBookedByCategory.put(categoryName, count.intValue());
        }
        return ticketsBookedByCategory;
    }



    

    
 
}
