package com.poortoys.examples.initializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.poortoys.examples.dao.PerformerDAO;
import com.ticketing.system.entities.Performer;
import com.poortoys.examples.dao.GenreDAO;
import com.ticketing.system.entities.Genre;


/**
 * PerformerInitializer populates the performers collection with initial data.
 * It associates each performer with an existing genre using ObjectId.
 */
public class PerformerInitializer implements Initializer {

    private final PerformerDAO performerDAO;
    private final GenreDAO genreDAO;

    /**
     * Constructor to initialize PerformerInitializer with PerformerDAO and GenreDAO.
     */
    public PerformerInitializer(PerformerDAO performerDAO, GenreDAO genreDAO) {
        this.performerDAO = performerDAO;
        this.genreDAO = genreDAO;
    }

    /**
     * Initializes the performers collection by adding sample performers.
     * Each performer is associated with a genre using ObjectId.
     */
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

        // Iterate and add performers
        for (String[] performerData : performersWithGenres) {
            String performerName = performerData[0];
            String genresString = performerData[1];

         // Split genre names by comma and trim spaces
            String[] genreNames = genresString.split(",");
            List<Genre> genres = new ArrayList<>();

            // Fetch Genre objects by name
            for (String genreName : genreNames) {
                Genre genre = genreDAO.findByName(genreName.trim());
                if (genre != null) {
                    genres.add(genre);
                } else {
                    System.out.println("Genre not found for performer: " + performerName + " - Genre: " + genreName);
                }
            }

            if (!genres.isEmpty()) {
                // Check if performer already exists
                if (!performerDAO.exists(performerName)) {
                    // Create Performer with list of Genre objects
                    Performer performer = new Performer(performerName, genres);
                    performerDAO.create(performer);
                    System.out.println("Added performer: " + performerName + " under genres " + genresString);
                } else {
                    System.out.println("Performer already exists: " + performerName);
                }
            } else {
                System.out.println("No valid genres found for performer: " + performerName);
            }
        }
        System.out.println("Performers initialization completed.\n");
    }
}
