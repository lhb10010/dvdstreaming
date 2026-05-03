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

    @PostMapping(value = "/video/{vidid}")
    public ResponseEntity<byte[]> partialVideoRequest(@PathVariable long vidid, @RequestBody JsonNode json){


        String target = json.get("target").asString();
        Video vid = videoRepository.findVideoById(vidid);
        byte[] data = new byte[0];

        if(target.equals("moov")){
            data = vid.getInitData();
        }

        else if(target.equals("frag")){

            int num = json.get("num").asInt();
            data = vid.getFragment(num);

        }

        //System.out.println(fragCount);
        //System.out.println(Arrays.toString(data));

        //create and send response
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header("Content-Type", "video/mp4")
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", String.valueOf(data.length))
                .body(data);


    }



}
