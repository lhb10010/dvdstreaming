package com.burkhead.dvdstreaming.controller;

import com.burkhead.dvdstreaming.model.Movie;
import com.burkhead.dvdstreaming.repository.MovieRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

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
        model.addAttribute("movie", targetMovie);

        if(!targetMovie.getMovieVideo().isReady()){
            return "movieProcessing";
        }
        else {
            return "moviePage";
        }
    }


    @PostMapping(path = "/movie/{movie}/watchTime")
    public ResponseEntity<String> updateWatchTime(@RequestBody JsonNode json, @PathVariable long movie){


        //TODO check for elapsed
        long num = json.get("elapsed").asLong();
        Movie m = movieRepository.findMovieById(movie);
        m.setLastTimeWatchedPos(num);
        movieRepository.save(m);

        String data = "OK";
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);

    }


    @GetMapping("/movie/{movie}/image")
    public ResponseEntity<byte[]> movieImage(@PathVariable long movie) {

        Movie targetMovie = movieRepository.findMovieById(movie);
        byte[] data = targetMovie.getThumbnail();

        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "application/octet-stream;")
                .header("Content-Length", String.valueOf(data.length))
                .body(data);

    }


}
