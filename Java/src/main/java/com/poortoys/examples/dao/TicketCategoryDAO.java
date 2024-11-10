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
     */
    public List<TicketCategory> findByEventId(Integer eventId) {
        TypedQuery<TicketCategory> query = em.createQuery(
            "SELECT tc FROM TicketCategory tc WHERE tc.event.eventId = :eventId", TicketCategory.class);
        query.setParameter("eventId", eventId);
        return query.getResultList();
    }

    /**
     * rtrieves all TicketCategories from the database.
     */
    public List<TicketCategory> findAll() {
        TypedQuery<TicketCategory> query = em.createQuery("SELECT tc FROM TicketCategory tc", TicketCategory.class);
        return query.getResultList();
    }

    /**
     * persists a new TicketCategory into the database.
     */
    public void create(TicketCategory ticketCategory) {
        em.persist(ticketCategory);
    }

    /**
     * Updates an existing TicketCategory in the database.
     */
    public TicketCategory update(TicketCategory ticketCategory) {
        return em.merge(ticketCategory);
    }

    /**
     * Deletes a TicketCategory from the database.
     */
    public void delete(TicketCategory ticketCategory) {
        em.remove(em.contains(ticketCategory) ? ticketCategory : em.merge(ticketCategory));
    }

    /**
     * Counts the total number of TicketCategories.
     */
    public Long count() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(tc) FROM TicketCategory tc", Long.class);
        return query.getSingleResult();
    }
}
