package com.burkhead.dvdstreaming.controller;

import com.burkhead.dvdstreaming.model.*;
import com.burkhead.dvdstreaming.repository.MovieRepository;
import com.burkhead.dvdstreaming.repository.ProcessingVideoRepository;
import com.burkhead.dvdstreaming.repository.TvSeriesRepository;
import com.burkhead.dvdstreaming.repository.VideoRepository;
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


@Controller
public class UploadSeriesController {

    final int MAX_TITLE_LENGTH = 50; //TODO make this a config
    private final VideoRepository videoRepository;
    private final ProcessingVideoRepository processingVideoRepository;
    private final TvSeriesRepository tvSeriesRepository;

    public UploadSeriesController(VideoRepository videoRepository, ProcessingVideoRepository processingVideoRepository, TvSeriesRepository tvSeriesRepository) {
        this.videoRepository = videoRepository;
        this.processingVideoRepository = processingVideoRepository;
        this.tvSeriesRepository = tvSeriesRepository;
    }

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


    @PostMapping(value = "/upload/series")
    public ResponseEntity<String> uploadSeries(@RequestBody JsonNode json){

        String data = "";
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);


    }

    @PostMapping(value = "/upload/episode")
    public ResponseEntity<String> uploadEpisode(@RequestBody JsonNode json){

        TvEpisode e = new TvEpisode();


        String data = String.valueOf(e.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);
    }
}
