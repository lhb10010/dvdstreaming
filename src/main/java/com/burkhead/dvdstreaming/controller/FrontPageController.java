package com.burkhead.dvdstreaming.controller;

import com.burkhead.dvdstreaming.repository.MovieRepository;
import com.burkhead.dvdstreaming.model.Movie;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
public class FrontPageController {

    private static final int n = 10;

    private final MovieRepository movies;

    public FrontPageController(MovieRepository movieRepository) {
        this.movies = movieRepository;
    }

    @GetMapping("/")
    public String frontPage(Model model){

        int titlesPerSection = n;

        //create genre sections
        ArrayList<String> genres = getGenres();
        ArrayList<ArrayList<Movie>> genresPicks = new ArrayList<ArrayList<Movie>>();
        for(String genre: genres){
            genresPicks.add(getNRandomMoviesFromGenre(genre, titlesPerSection));
        }

        System.out.println("here2");

        //create least watched
        ArrayList<Movie> lastTimeWatchedDescending = (ArrayList<Movie>) this.movies.findAll(Sort.by(Sort.Direction.DESC, "lastTimeWatched"));
        ArrayList<Movie> lastTimeWatchedFinal = new ArrayList<Movie>();

        System.out.println("here3");

        int j = 0;
        for(int i = 0; i < titlesPerSection; i++){
            lastTimeWatchedFinal.add(lastTimeWatchedDescending.get(j));

            j++;
            if(j >= lastTimeWatchedDescending.size()){
                j = 0;
            }
        }

        System.out.println("here4");

        ArrayList<ArrayList<Movie>> allMovieLists = new ArrayList<>(genresPicks);
        allMovieLists.add(lastTimeWatchedDescending);
        allMovieLists.add(lastTimeWatchedFinal);

        System.out.println("here5");

        ArrayList<String> listNames = new ArrayList<>();
        listNames.add("Action Movies");
        listNames.add("Not Watched In A While");
        listNames.add("Another One");

        System.out.println("here6");

        model.addAttribute("allMovieLists", allMovieLists);
        model.addAttribute("listNames", listNames); //for testing


        return "frontPage";

    }


    public ArrayList<String> getGenres(){
        ArrayList<String> genres = new ArrayList<String>();
        genres.add("action");
        return genres;
    }

    public ArrayList<Movie> getNRandomMoviesFromGenre(String genre, int n){

        Movie search = new Movie();
        search.setGenre(genre);
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase().withIgnorePaths("id", "lastTimeWatchedPos", "movieVideo", "lastTimeWatched");
        Example<Movie> targetGenreExample = Example.of(search, matcher);
        ArrayList<Movie> genreMovies = (ArrayList<Movie>) this.movies.findAll(targetGenreExample);

        return getNRandomMovie(genreMovies, n);

    }


    //placeholder
    public ArrayList<Movie> getNRandomMovie(ArrayList<Movie> movieList, int num){

        ArrayList<Movie> finalList = new ArrayList<Movie>();

        int j = 0;
        for(int i = 0; i < num; i++){
            Movie target = movieList.get(j);
            finalList.add(target);

            j++;
            if(j >= movieList.size()){
                j = 0;
            }
        }

        return finalList;

    }

}


