package com.burkhead.dvdstreaming.controller;

import com.burkhead.dvdstreaming.model.Movie;
import com.burkhead.dvdstreaming.model.Video;
import com.burkhead.dvdstreaming.repository.MovieRepository;
import com.burkhead.dvdstreaming.repository.VideoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.databind.JsonNode;

import java.util.Base64;


@Controller
public class UploadMovieController {

    final int MAX_TITLE_LENGTH = 50; //TODO make this a config
    private final VideoRepository videoRepository;
    private final MovieRepository movieRepository;

    public UploadMovieController(VideoRepository videoRepository, MovieRepository movieRepository) {
        this.videoRepository = videoRepository;
        this.movieRepository = movieRepository;
    }

    @GetMapping("/uploadMovie")
    public String uploadMoviePage(Model model){
        return "uploadMovie";
    }


    @PostMapping(value = "/upload/movie")
    public ResponseEntity<String> partialVideoRequest(@RequestBody JsonNode json){


        //check that all fields are present
        if(!(json.hasNonNull("title") && json.hasNonNull("image") &&
                json.hasNonNull("genre") && json.hasNonNull("video"))){
            //TODO 400
        }


        //check title
        String title = json.get("title").asString();
        if(title.length() > MAX_TITLE_LENGTH){
            //TODO 400
        }


        //TODO list of genres
        String genre = json.get("genre").asString();
        if(genre.length() > 25){
            //TODO 400
        }

        String image = json.get("image").asString();
        byte[] decodedImage = Base64.getDecoder().decode(image);

        long video = json.get("video").asLong();
        //TODO check video exists

        Video v = videoRepository.findVideoById(video);

        //create new movie object
        Movie m = new Movie(title, genre, decodedImage, v);

        movieRepository.save(m);

        String data = String.valueOf(m.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);


    }
}
