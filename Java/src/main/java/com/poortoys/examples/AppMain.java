package com.poortoys.examples;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
//import com.example.entities.Genre;
import java.util.List;

public class AppMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ticketingsystem");
        EntityManager em = emf.createEntityManager();
        
        try {
        	List<Genre> genres = em.createQuery("SELECT g FROM Genre g", Genre.class).getResultList();
        	genres.forEach(genre -> System.out.println(genre.getGenreName()));
        }finally {
        	em.close();
        	emf.close();
        }
        
    }

}
