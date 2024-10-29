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

	//Persists a new entity instance into the database
	public void create(Genre genre) {
		// TODO Auto-generated method stub
		em.persist(genre);
		
	}
	
	/*
	 * Counts the total number of Genres
	 * @return Total count of genres
	 */
	public Integer count() {
		TypedQuery<Integer> query = em.createQuery("SELECT COUNT(g) FROM Genre g", Integer.class);
		return query.getSingleResult();
	}

}
