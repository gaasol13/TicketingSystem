package com.poortoys.examples.dao;

import com.ticketing.system.entities.Event;
import dev.morphia.Datastore;
import dev.morphia.query.experimental.filters.Filters;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for Event entities in MongoDB.
 */
public class EventDAO {

    private final Datastore datastore;

    /**
     * Constructor injecting the Morphia Datastore.
     */
    public EventDAO(Datastore datastore) {
        this.datastore = datastore;
    }

    /**
     * Finds an Event by its name and date.
     */
    public Event findByNameAndDate(String name, Date date) {
        return datastore.find(Event.class)
                .filter(Filters.eq("name", name), Filters.eq("date", date))
                .first();
    }

    /**
     * Finds an Event by its ObjectId.
     */
    public Event findById(ObjectId id) {
        return datastore.find(Event.class)
                .filter(Filters.eq("_id", id))
                .first();
    }

    /**
     * Retrieves all Events from the database.
     */
    public List<Event> findAll() {
        return datastore.find(Event.class)
                .iterator()
                .toList();
    }

    /**
     * Persists a new Event into the database.
     */
    public void create(Event event) {
        datastore.save(event);
    }

    /**
     * Updates an existing Event in the database.
     */
    public void update(Event event) {
        datastore.save(event); // Morphia's save() updates if the entity has an id
    }

    /**
     * Deletes an Event from the database.
     */
    public void delete(Event event) {
        datastore.delete(event);
    }

    /**
     * Counts the total number of Events.
     */
    public long count() {
        return datastore.find(Event.class).count();
    }
}