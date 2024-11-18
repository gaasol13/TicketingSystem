package com.poortoys.examples.dao;

import com.poortoys.examples.entities.Ticket;
import com.poortoys.examples.entities.TicketStatus;
import com.poortoys.examples.entities.User;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import java.util.List;

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
    
 
}
