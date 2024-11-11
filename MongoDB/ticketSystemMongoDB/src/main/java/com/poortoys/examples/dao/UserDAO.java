package com.poortoys.examples.dao;

import java.util.List;

import com.ticketing.system.entities.User;

import dev.morphia.Datastore;

public class UserDAO {
	
	//morphias datastore for interacting with MongoDB
	private final Datastore datastore;
	
    public UserDAO(Datastore datastore) {
        this.datastore = datastore;
    }
	
	
	//find the user by email
	public User findByEmail(String email) {
		return datastore.find(User.class)
				.filter(dev.morphia.query.experimental.filters.Filters.eq("email", email))
				.first();
	}
	
	//find the user by username
	public User findByUserName(String userName) {
		return datastore.find(User.class)
				.filter(dev.morphia.query.experimental.filters.Filters.eq("userName", userName))
				.first();
	}
	
	//find all
	public List<User> findAll(){
		return datastore.find(User.class).iterator().toList();
	}
	
	//create a new userName
	public void create(User user) {
		datastore.save(user);
	}
	
	//count the total number of performers
	public long count() {
		return datastore.find(User.class).count();
	}
	
	//delete a user
	public void delete(User user) {
		datastore.delete(user);
	}
	
	//check if the user already exists
	public boolean existsByEmail(String email) {
		return findByEmail(email) != null;
	}
	
	public boolean existsByUserName(String userName) {
		return findByUserName(userName) != null;
	}
	
	public void update(User user) {
		datastore.save(user);
	}

}
