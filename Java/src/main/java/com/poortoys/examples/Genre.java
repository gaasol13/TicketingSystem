package com.poortoys.examples;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity //Specifies that the class is an entity and is mapped to a database table
@Table(name = "genres") //Maps the entity to the tickets table
public class Genre {
	
	//Column >ID 
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) //Denotes the primary key and its generation strategy
	@Column(name = "genre_id")//Maps class fields to table columns
	private int genreId;
	
	//
	@Column(name = "genre_name", nullable = false, length = 35)
	private String genreName;

	//Constructors
	public Genre() {
	}

	public Genre(String genreName) {
		this.genreName = genreName;
	}
	
	//Getters and Setters
	
	public int getGenreId() {
		return genreId;
	}

	public String getGenreName() {
		return genreName;
	}

	public void setGenreName(String genreName) {
		this.genreName = genreName;
	}
	
	//Override
	@Override
	public String toString() {
		return "Genre [genreId=" + genreId + ", genreName=" + genreName + "]";
	}
	

}
