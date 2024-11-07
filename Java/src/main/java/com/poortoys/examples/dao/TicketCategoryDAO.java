package com.poortoys.examples.dao;

import com.poortoys.examples.entities.Event;
import com.poortoys.examples.entities.TicketCategory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class TicketCategoryDAO {

    private EntityManager em;

    public TicketCategoryDAO(EntityManager em) {
        this.em = em;
    }

    /**
     * Finds all TicketCategories associated with a particular event.
     * @param event ID of the event.
     * @return List of TicketCategory objects.
     */
    public List<TicketCategory> findByEventId(Integer eventId) {
        TypedQuery<TicketCategory> query = em.createQuery(
            "SELECT tc FROM TicketCategory tc WHERE tc.event.eventId = :eventId", TicketCategory.class);
        query.setParameter("eventId", eventId);
        return query.getResultList();
    }

    /**
     * Retrieves all TicketCategories from the database.
     * @return A list of all TicketCategory objects.
     */
    public List<TicketCategory> findAll() {
        TypedQuery<TicketCategory> query = em.createQuery("SELECT tc FROM TicketCategory tc", TicketCategory.class);
        return query.getResultList();
    }

    /**
     * Persists a new TicketCategory into the database.
     * @param ticketCategory The TicketCategory object to be saved.
     */
    public void create(TicketCategory ticketCategory) {
        em.persist(ticketCategory);
    }

    /**
     * Updates an existing TicketCategory in the database.
     * @param ticketCategory The TicketCategory object with updated information.
     * @return The updated TicketCategory object.
     */
    public TicketCategory update(TicketCategory ticketCategory) {
        return em.merge(ticketCategory);
    }

    /**
     * Deletes a TicketCategory from the database.
     * @param ticketCategory The TicketCategory object to delete.
     */
    public void delete(TicketCategory ticketCategory) {
        em.remove(em.contains(ticketCategory) ? ticketCategory : em.merge(ticketCategory));
    }

    /**
     * Counts the total number of TicketCategories.
     * @return Total count of ticket categories.
     */
    public Long count() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(tc) FROM TicketCategory tc", Long.class);
        return query.getSingleResult();
    }
}
