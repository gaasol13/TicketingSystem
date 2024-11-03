package com.poortoys.examples.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import com.poortoys.examples.entities.Performer;

public class PerformerDAO {

    private EntityManager em;

    public PerformerDAO(EntityManager em) {
        this.em = em;
    }

    /**
     * Finds a Performer by its name.
     * @param name Name of the performer.
     * @return Performer object if found, else null.
     */
    public Performer findByName(String name) {
        TypedQuery<Performer> query = em.createQuery(
            "SELECT p FROM Performer p WHERE p.performerName = :name", Performer.class);
        query.setParameter("name", name);
        List<Performer> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Retrieves all Performers from the database.
     * @return A list of all Performer objects.
     */
    public List<Performer> findAll() {
        TypedQuery<Performer> query = em.createQuery("SELECT p FROM Performer p", Performer.class);
        return query.getResultList();
    }

    /**
     * Persists a new Performer into the database.
     * @param performer The Performer object to be saved.
     */
    public void create(Performer performer) {
        em.persist(performer);
    }

    /**
     * Updates an existing Performer in the database.
     * @param performer The Performer object with updated information.
     * @return The updated Performer object.
     */
    public Performer update(Performer performer) {
        return em.merge(performer);
    }

    /**
     * Deletes a Performer from the database.
     * @param performer The Performer object to delete.
     */
    public void delete(Performer performer) {
        em.remove(em.contains(performer) ? performer : em.merge(performer));
    }

    /**
     * Counts the total number of Performers.
     * @return Total count of performers.
     */
    public Long count() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(p) FROM Performer p", Long.class);
        return query.getSingleResult();
    }
}
