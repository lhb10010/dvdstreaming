package com.burkhead.dvdstreaming.controller;

import com.burkhead.dvdstreaming.model.Media;
import com.burkhead.dvdstreaming.repository.MediaRepository;
import com.burkhead.dvdstreaming.repository.VideoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;

@Controller
public class SearchPageController {

    public final MediaRepository mediaRepository;

    public SearchPageController(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    @GetMapping("/Search?q=<query>")
    public ResponseEntity<String> searchPage(Model model, @PathVariable String query){


        //ArrayList<Media> finds = (ArrayList<Media>) mediaRepository.findByTitleContaining("query");
        //model.addAttribute("finds", finds);

        return null;

    }


}
