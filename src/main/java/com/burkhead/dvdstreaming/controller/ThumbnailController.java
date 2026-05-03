package com.burkhead.dvdstreaming.controller;

import com.burkhead.dvdstreaming.repository.MediaThumbnailRepository;
import com.burkhead.dvdstreaming.repository.MovieRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ThumbnailController {

    private final MediaThumbnailRepository thumbnails;

    //constructor

    public ThumbnailController(MediaThumbnailRepository thumbnails) {
        this.thumbnails = thumbnails;
    }

    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<byte[]> image(@PathVariable Long id){

        byte[] imageBytes = thumbnails.getReferenceById(id).getImageData();
        HttpHeaders headers = new HttpHeaders();

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
}
