package com.poortoys.examples;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.poortoys.examples.dao.GenreDAO;
import com.poortoys.examples.entities.Genre;
import com.poortoys.examples.initilizer.DataInitializer;

//import com.example.entities.Genre;
import java.util.List;

public class AppMain {

    public static void main(String[] args) {

    	
		
		
		 DataInitializer initializer = new DataInitializer();
		 initializer.populateData(); 
		 initializer.close();
		 
    	
		   
    	
        
    }

}
