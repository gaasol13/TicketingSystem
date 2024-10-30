package com.poortoys.examples;

import com.mongodb.client.MongoClient;

public class AppMain {

    public static void main(String[] args) {
        try {
        	MongoClient mongoClient = MongoDBConnection.getMongoClient();
        	
        	MongoDBConnection.close();
        }catch(Exception e) {
        	System.err.println("Error");
        	e.printStackTrace();
        }
    }

}
