package com.burkhead.dvdstreaming.controller;

import com.burkhead.dvdstreaming.model.Video;
import com.burkhead.dvdstreaming.repository.VideoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.databind.JsonNode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

@Controller
public class VideoController {

    private final VideoRepository videoRepository;

    public VideoController(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @PostMapping(value = "/video/{vidid}/getInitData")
    public ResponseEntity<byte[]> videoInitData(@PathVariable long vidid){


        Video vid = videoRepository.findVideoById(vidid);
        byte[] data = vid.getInitData();

        //create and send response
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "video/mp4")
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", String.valueOf(data.length))
                .body(data);

    }


    @PostMapping(value = "/video/{vidid}/getChunk")
    public ResponseEntity<byte[]> videoChunkData(@PathVariable long vidid, @RequestBody JsonNode json){


        //TODO check for num
        Video vid = videoRepository.findVideoById(vidid);
        int num = json.get("num").asInt();
        byte[] data = vid.getFragment(num);

        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "video/mp4")
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", String.valueOf(data.length))
                .body(data);

    }



}
