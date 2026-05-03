package com.burkhead.dvdstreaming.controller;

import com.burkhead.dvdstreaming.model.Movie;
import com.burkhead.dvdstreaming.repository.MovieRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;

@Controller
public class MoviePageController {

    public final MovieRepository movieRepository;

    public MoviePageController(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @GetMapping("/movie/{movie}")
    public String frontPage(Model model, @PathVariable long movie) {

        Movie targetMovie = movieRepository.findMovieById(movie);
        System.out.println(targetMovie.getGenre());
        System.out.println(targetMovie.getVideoId());
        model.addAttribute("movie", targetMovie);

        return "moviePage";
    }


}
