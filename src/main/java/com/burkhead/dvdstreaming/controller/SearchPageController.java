package com.burkhead.dvdstreaming.controller;

import com.burkhead.dvdstreaming.model.Media;
import com.burkhead.dvdstreaming.repository.MediaRepository;
import com.burkhead.dvdstreaming.repository.MovieRepository;
import com.burkhead.dvdstreaming.repository.TvSeriesRepository;
import com.burkhead.dvdstreaming.repository.VideoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;

@Controller
public class SearchPageController {

    public final MediaRepository mediaRepository;
    public final MovieRepository movieRepository;
    public final TvSeriesRepository tvSeriesRepository;

    public SearchPageController(MovieRepository movieRepository, TvSeriesRepository tvSeriesRepository) {
        this.movieRepository = movieRepository;
        this.tvSeriesRepository = tvSeriesRepository;
        this.mediaRepository = new MediaRepository(tvSeriesRepository, movieRepository);
    }

    @GetMapping("/search")
    public String searchPage(Model model, @RequestParam String q){


        ArrayList<Media> finds = mediaRepository.findByTitleContaining(q);
        model.addAttribute("finds", finds);

        return "search";

    }


}
