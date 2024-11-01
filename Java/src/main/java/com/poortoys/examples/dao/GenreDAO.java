package com.poortoys.examples.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.poortoys.examples.entities.Genre;

public class GenreDAO {
	
	private EntityManager em;

	public GenreDAO(EntityManager em) {
		// TODO Auto-generated constructor stub
		this.em = em;
	}
	
	/* Finds a Genre by its name.
    * @param name Name of the genre.
    * @return Genre object if found, else null.
    */
	public Genre findByName(String name) {
		// TODO Auto-generated method stub
		//Query Creation
		TypedQuery<Genre> query = em.createQuery("SELECT g FROM Genre g WHERE g.genreName = :name",
				Genre.class);//parameter Genre name
		query.setParameter("name", name);
		
		List<Genre> results = query.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}
	
	  public List<Genre> findAll() {
	        TypedQuery<Genre> query = em.createQuery("SELECT g FROM Genre g", Genre.class);
	        return query.getResultList();
	    }
	
	
	//Persists a new entity instance into the database
	public void create(Genre genre) {
		// TODO Auto-generated method stub
		em.persist(genre);
			
	}
	
	public Genre update(Genre genre) {
		return em.merge(genre);
	}
	
	public void delete(Genre genre) {
		em.remove(em.contains(genre)? genre : em.merge(genre));
	}
	
	
	
	/*
	 * Counts the total number of Genres
	 * @return Total count of genres
	 */
	public Long count() {
		TypedQuery<Long> query = em.createQuery("SELECT COUNT(*) FROM Genre", Long.class);
		return query.getSingleResult();
	}

}
