package com.poortoys.examples.initilizer;

import com.poortoys.examples.dao.PerformerDAO;
import com.poortoys.examples.dao.GenreDAO;
import com.poortoys.examples.entities.Performer;
import com.poortoys.examples.entities.Genre;

import java.util.Arrays;
import java.util.List;

public class PerformerInitializer implements Initializer {

    private PerformerDAO performerDAO;
    private GenreDAO genreDAO;

    public PerformerInitializer(PerformerDAO performerDAO, GenreDAO genreDAO) {
        this.performerDAO = performerDAO;
        this.genreDAO = genreDAO;
    }

    @Override
    public void initialize() {
        System.out.println("Initializing performers...");

        // List of performer names and their associated genre names
        List<String[]> performersWithGenres = Arrays.asList(
            new String[]{"The Rockers", "Rock"},
            new String[]{"Jazz Masters", "Jazz"},
            new String[]{"Classical Quartet", "Classical"},
            new String[]{"Pop Icons", "Pop"},
            new String[]{"Electronica", "Electronic"},
            new String[]{"Hip-Hop Crew", "Hip-Hop"},
            new String[]{"Country Stars", "Country"},
            new String[]{"Blues Band", "Blues"},
            new String[]{"Reggae Rhythms", "Reggae"},
            new String[]{"Metal Heads", "Metal"}
        );

        // Iterate over the list and add performers with their genres
        for (String[] performerData : performersWithGenres) {
            String performerName = performerData[0];
            String genreName = performerData[1];

            // Find the genre by name
            Genre genre = genreDAO.findByName(genreName);

            if (genre != null) {
                // Check if the performer already exists
                if (performerDAO.findByName(performerName) == null) {
                    // Create a new Performer associated with the found Genre
                    Performer performer = new Performer(performerName, genre);
                    performerDAO.create(performer);
                    System.out.println("Added performer: " + performerName + " under genre " + genreName);
                } else {
                    System.out.println("Performer already exists: " + performerName);
                }
            } else {
                System.out.println("Genre not found for performer: " + performerName);
            }
        }
        System.out.println("Performers initialization completed.\n");
    }
}
