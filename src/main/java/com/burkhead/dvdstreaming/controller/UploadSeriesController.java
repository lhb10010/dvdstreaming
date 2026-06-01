package com.burkhead.dvdstreaming.controller;

import com.burkhead.dvdstreaming.model.*;
import com.burkhead.dvdstreaming.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Optional;


@Controller
public class UploadSeriesController {

    final int MAX_TITLE_LENGTH = 50; //TODO make this a config
    private final VideoRepository videoRepository;
    private final ProcessingVideoRepository processingVideoRepository;
    private final TvSeriesRepository tvSeriesRepository;
    private final TvEpisodeRepository tvEpisodeRepository;
    private final TvSeasonRepository tvSeasonRepository;

    public UploadSeriesController(VideoRepository videoRepository, ProcessingVideoRepository processingVideoRepository, TvSeriesRepository tvSeriesRepository, TvEpisodeRepository tvEpisodeRepository, TvSeasonRepository tvSeasonRepository) {
        this.videoRepository = videoRepository;
        this.processingVideoRepository = processingVideoRepository;
        this.tvSeriesRepository = tvSeriesRepository;
        this.tvEpisodeRepository = tvEpisodeRepository;
        this.tvSeasonRepository = tvSeasonRepository;
    }


    // ---------------------------------------------- Get Pages ----------------------------------------------


    @GetMapping("/uploadSeason")
    public String uploadSeasonPage(Model model){

        ArrayList<TvSeries> allSeries = (ArrayList<TvSeries>) tvSeriesRepository.findAll();

        model.addAttribute("all", allSeries); //TODO make only name and ID

        return "uploadSeason";
    }


    @GetMapping("/uploadSeries")
    public String uploadSeriesPage(Model model){
        return "uploadSeries";
    }


    // ---------------------------------------------- Post APIs ----------------------------------------------


    @PostMapping(value = "/upload/series")
    public ResponseEntity<String> uploadSeries(@RequestBody JsonNode json){


        if(!(json.hasNonNull("title") && json.hasNonNull("image") &&
                json.hasNonNull("genre"))){
            //TODO 400
        }


        //check title
        String title = json.get("title").asString();
        if(title.length() > MAX_TITLE_LENGTH){
            //TODO 400
        }


        //TODO list of genres
        String genre = json.get("genre").asString();
        if(genre.length() > 25){ //TODO config
            //TODO 400
        }

        String image = json.get("image").asString();
        System.out.println(image);
        byte[] decodedImage = Base64.getDecoder().decode(image);

        TvSeries t = new TvSeries(title, genre, decodedImage);
        tvSeriesRepository.save(t);


        String data = Long.toString(t.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);


    }

    @PostMapping(value = "/upload/episode")
    public ResponseEntity<String> uploadEpisode(@RequestBody JsonNode json){


        if(!(json.hasNonNull("title") && json.hasNonNull("num")
                && json.hasNonNull("season"))){
            //TODO 400
        }


        long season = json.get("season").asLong();
        TvSeason s = tvSeasonRepository.findTvSeasonById(season);
        if(s == null){
            //TODO 400
        }


        String title = json.get("title").asString();
        if(title.length() > MAX_TITLE_LENGTH){
            //TODO 400
        }


        int episodeNum = json.get("num").asInt();
        if(episodeNum > 100){
            //TODO 400
        }

        TvEpisode e = new TvEpisode(title, episodeNum, s);
        tvEpisodeRepository.save(e);


        String data = String.valueOf(e.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);
    }


    @PostMapping(value = "/upload/season")
    public ResponseEntity<String> uploadSeason(@RequestBody JsonNode json){


        if(!(json.hasNonNull("series") && json.hasNonNull("num"))){
            //TODO 400
        }


        long series = json.get("series").asLong();
        TvSeries s = tvSeriesRepository.findTvSeriesById(series);
        if(s == null){
            //TODO 400
        }


        int num = json.get("num").asInt();
        if(num > 100 || num < 1){
            //TODO 400
        }

        TvSeason season = new TvSeason(num, s);
        tvSeasonRepository.save(season);


        String data = String.valueOf(season.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);
    }



    @PostMapping(value = "/upload/episode/{episodeNum}/applyvideo")
    public ResponseEntity<String> applyVideoToEpisode(@RequestBody JsonNode json){


        if(!(json.hasNonNull("series") && json.hasNonNull("num"))){
            //TODO 400
        }


        long series = json.get("series").asLong();
        TvSeries s = tvSeriesRepository.findTvSeriesById(series);
        if(s == null){
            //TODO 400
        }


        int num = json.get("num").asInt();
        if(num > 100 || num < 1){
            //TODO 400
        }

        TvSeason season = new TvSeason(num, s);
        tvSeasonRepository.save(season);


        String data = String.valueOf(season.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);
    }
}
