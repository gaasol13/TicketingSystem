package com.poortoys.examples.dao;

import com.poortoys.examples.entities.Event;
import com.poortoys.examples.entities.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class UserDAO {

    private EntityManager em;

    public UserDAO(EntityManager em) {
        this.em = em;
    }

    /**
     * Finds a User by their email.
     * @param email Email of the user.
     * @return User object if found, else null.
     */
    public User findByEmail(String email) {
        TypedQuery<User> query = em.createQuery(
            "SELECT u FROM User u WHERE u.email = :email", User.class);
        query.setParameter("email", email);
        List<User> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    public User findById(int id) {
        return em.find(User.class, id);
    }

    /**
     * Retrieves all Users from the database.
     */
    public List<User> findAll() {
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
        return query.getResultList();
    }
    
    public User findByUsername(String username) {
        TypedQuery<User> query = em.createQuery(
            "SELECT u FROM User u WHERE u.userName = :username", User.class);
        query.setParameter("username", username);
        List<User> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }


    /**
     * Persists a new User into the database.
     * @param user The User object to be saved.
     */
    public void create(User user) {
        em.persist(user);
    }

    /**
     * Updates an existing User in the database.
     * @param user The User object with updated information.
     * @return The updated User object.
     */
    public User update(User user) {
        return em.merge(user);
    }

    /**
     * Deletes a User from the database.
     * @param user The User object to delete.
     */
    public void delete(User user) {
        em.remove(em.contains(user) ? user : em.merge(user));
    }

    /**
     * Counts the total number of Users.
     * @return Total count of users.
     */
    public Long count() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM User u", Long.class);
        return query.getSingleResult();
    }
}
