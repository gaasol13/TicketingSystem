package com.poortoys.examples.entities;

import javax.persistence.*;

import java.util.Date;


@Entity //Specifies that the class is an entity and is mapped to a database table
@Table(name = "performers")
public class Performer {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "performer_id")
	private int performerId;
	
	@Column(name = "performer_name", nullable = false, length = 100)
	private String performerName;
	
	//Since performer has a relationship with GEnre, I created a many to one relationship
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "genre_id")
	private Genre genre;

	
	//Constructor for convenience
	public Performer() {
	}

	public Performer(String performerName, Genre genre) {
		this.performerName = performerName;
		this.genre = genre;
	}
	
	//Getter and setter (no setter for performerId since it's auto-generated
	
	public int getPerformerId() {
		return performerId;
	}


	public String getPerformerName() {
		return performerName;
	}

	public void setPerformerName(String performerName) {
		this.performerName = performerName;
	}

	public Genre getGenre() {
		return genre;
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}

	@Override
	public String toString() {
		return "Performer [performerId=" + performerId + ", performerName=" + performerName + ", genre=" + genre + "]";
	}
	
	

	
}
