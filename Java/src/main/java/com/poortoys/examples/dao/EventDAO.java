package com.poortoys.examples.dao;

import com.poortoys.examples.entities.Event;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class EventDAO {

    private EntityManager em;

    public EventDAO(EntityManager em) {
        this.em = em;
    }

    /**
     * Finds an Event by its name and date.
     * @param name Name of the event.
     * @param eventDate Date of the event.
     * @return Event object if found, else null.
     */
    public Event findByNameAndDate(String name, Date eventDate) {
        TypedQuery<Event> query = em.createQuery(
            "SELECT e FROM Event e WHERE e.eventName = :name AND FUNCTION('DATE', e.eventDate) = FUNCTION('DATE', :date)", Event.class);
        query.setParameter("name", name);
        query.setParameter("date", eventDate);
        List<Event> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public Event findById(int id) {
        return em.find(Event.class, id);
    }


    /**
     * Retrieves all Events from the database.
     * @return A list of all Event objects.
     */
    public List<Event> findAll() {
        TypedQuery<Event> query = em.createQuery("SELECT e FROM Event e", Event.class);
        return query.getResultList();
    }

    /**
     * Persists a new Event into the database.
     * @param event The Event object to be saved.
     */
    public void create(Event event) {
        em.persist(event);
    }

    /**
     * Updates an existing Event in the database.
     * @param event The Event object with updated information.
     * @return The updated Event object.
     */
    public Event update(Event event) {
        return em.merge(event);
    }

    /**
     * Deletes an Event from the database.
     * @param event The Event object to delete.
     */
    public void delete(Event event) {
        em.remove(em.contains(event) ? event : em.merge(event));
    }

    /**
     * Counts the total number of Events.
     * @return Total count of events.
     */
    public Long count() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(e) FROM Event e", Long.class);
        return query.getSingleResult();
    }
}
