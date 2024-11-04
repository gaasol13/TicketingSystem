package com.poortoys.examples.dao;

import com.poortoys.examples.entities.Venue;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class VenueDAO {

    private EntityManager em;

    public VenueDAO(EntityManager em) {
        this.em = em;
    }

    /**
     * Finds a Venue by its name.
     * @param name Name of the venue.
     * @return Venue object if found, else null.
     */
    public Venue findByName(String name) {
        TypedQuery<Venue> query = em.createQuery(
            "SELECT v FROM Venue v WHERE v.venueName = :name", Venue.class);
        query.setParameter("name", name);
        List<Venue> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Retrieves all Venues from the database.
     * @return A list of all Venue objects.
     */
    public List<Venue> findAll() {
        TypedQuery<Venue> query = em.createQuery("SELECT v FROM Venue v", Venue.class);
        return query.getResultList();
    }

    /**
     * Persists a new Venue into the database.
     * @param venue The Venue object to be saved.
     */
    public void create(Venue venue) {
        em.persist(venue);
    }

    /**
     * Updates an existing Venue in the database.
     * @param venue The Venue object with updated information.
     * @return The updated Venue object.
     */
    public Venue update(Venue venue) {
        return em.merge(venue);
    }

    /**
     * Deletes a Venue from the database.
     * @param venue The Venue object to delete.
     */
    public void delete(Venue venue) {
        em.remove(em.contains(venue) ? venue : em.merge(venue));
    }

    /**
     * Counts the total number of Venues.
     * @return Total count of venues.
     */
    public Long count() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(v) FROM Venue v", Long.class);
        return query.getSingleResult();
    }
}
