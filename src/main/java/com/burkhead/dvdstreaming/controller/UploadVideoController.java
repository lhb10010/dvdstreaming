package com.burkhead.dvdstreaming.controller;

import com.burkhead.dvdstreaming.model.ProcessingVideo;
import com.burkhead.dvdstreaming.model.TvSeason;
import com.burkhead.dvdstreaming.model.Video;
import com.burkhead.dvdstreaming.model.VideoContainer;
import com.burkhead.dvdstreaming.repository.MovieRepository;
import com.burkhead.dvdstreaming.repository.ProcessingVideoRepository;
import com.burkhead.dvdstreaming.repository.TvEpisodeRepository;
import com.burkhead.dvdstreaming.repository.VideoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;

@Controller
public class UploadVideoController {

    private final long CHUNK_SIZE = 8192; //TODO make config

    public final VideoRepository videoRepository;
    public final ProcessingVideoRepository processingVideoRepo;
    public final MovieRepository movieRepository;
    public final TvEpisodeRepository tvEpisodeRepository;

    public UploadVideoController(VideoRepository videoRepository, ProcessingVideoRepository processingVideoController, MovieRepository movieRepository, TvEpisodeRepository tvEpisodeRepository) {
        this.videoRepository = videoRepository;
        this.processingVideoRepo = processingVideoController;
        this.movieRepository = movieRepository;
        this.tvEpisodeRepository = tvEpisodeRepository;
    }

    //step 1
    @PostMapping("/uploadVideo")
    public ResponseEntity<String> initChunkingVideoUpload(@RequestBody JsonNode json){


        //ensure required JSON values are present
        if(!(json.hasNonNull("totalSize") && json.hasNonNull("chunkCount"))){
            System.out.println("400:1");
            //TODO 400
        }

        long totalSize = json.get("totalSize").asLong();
        long chunkCount = json.get("chunkCount").asLong();


        //create a fileId for this file
        ProcessingVideo p = new ProcessingVideo(chunkCount, totalSize);
        processingVideoRepo.save(p);
        System.out.println(p.getId());

        //create new directory for temporary temp files
        p.createChunkFolder();
        processingVideoRepo.save(p);

        //setup response
        String data = String.valueOf(p.getId());
        System.out.println(data);
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);

    }


    // ------------------------------------------ upload chunks -------------------------------------------
    // ---------------------------------------------- step 2 ----------------------------------------------


    @PostMapping(value = "/uploadVideo/{fileid}", consumes = "application/octet-stream")
    public ResponseEntity<String> uploadChuck(@RequestBody byte[] chunkData,
                                              @RequestHeader("x-chunk-number") long chunkNum,
                                              @RequestHeader("x-total-chunk-count") long totalChunks,  //TODO make this server side
                                              @PathVariable long fileid){


        //check that chunk matches expected chunk size for all chinks except last
        if(chunkNum != totalChunks - 1 && chunkData.length != CHUNK_SIZE){
            System.out.println(chunkData.length);
            System.out.println("bad");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("data did not match chunk size");
        }
        //TODO also add error detection for last chunk


        //getProcessingVideo
        ProcessingVideo targetVid = processingVideoRepo.findProcessingVideoById(fileid);


        //write chunk
        if(!targetVid.addChunkToFolder(chunkData, chunkNum)){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Transaction Cancelled"); //TODO cancel transaction
        }

        //setup success response
        String data = "OK";
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);

    }


    @PostMapping("/uploadVideo/{fileid}/finish/")
    public ResponseEntity<String> finishUpload(@RequestHeader("x-total-chunk-count") long totalChunks,  //TODO make this server side
                                               @PathVariable long fileid){

        ProcessingVideo targetVid = processingVideoRepo.findProcessingVideoById(fileid);
        ArrayList<Long> missingChunks = targetVid.checkForMissingChunks();


        //tell client to send missing chunks if theres missing chunks
        if(!missingChunks.isEmpty()){
            StringBuilder data = new StringBuilder();
            data.append("missingChunks:");
            System.out.println("missing");
            for(long num : missingChunks){
                System.out.println(num);
                data.append(num);
                data.append(",");
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .header("Content-Length", String.valueOf(data.length()))
                    .body(data.toString());
        }

        //reconstruct and process into final video
        targetVid.reconstructVideoFile();
        targetVid.convertToFinalFMP4(processingVideoRepo, videoRepository);
        Video v = new Video(targetVid);
        videoRepository.save(v);


        String data = String.valueOf(v.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);

    }


    @GetMapping(value = "/uploadVideo/{fileid}/processingProgress")
    public ResponseEntity<String> getProgress(@PathVariable long fileid) {

        ProcessingVideo targetVid = processingVideoRepo.findProcessingVideoById(fileid); //TODO handle not found

        String data = "100";
        if(targetVid != null) {
            data = String.valueOf(targetVid.getFfmpegProcessingPercentage());
        }

        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);

    }


    /*
    @PostMapping(value = "/applyVideo")
    public ResponseEntity<String> applyVideoToVideoContainer(@RequestBody JsonNode json) {

        if(!(json.hasNonNull("containerId") && json.hasNonNull("type") && json.hasNonNull("videoId"))){
            //TODO 400
        }

        long containerId = json.get("containerId").asLong();
        String type = json.get("type").asString();
        if(type.equals("movie")){
            v = movieRepository.findMovieById(containerId);
        }
        else if(type.equals("tv")){
            v = tvEpisodeRepository.findTvEpisodeById(containerId);
        }
        else{
            //TODO 400
        }

        if(v == null){
            //TODO 400
        }

        long videoId = json.get("videoId").asLong();
        Video video = videoRepository.findVideoById(videoId);
        if(video == null){
            //TODO 400
        }

        v.setVideo(video);

        String data = "OK";
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Length", String.valueOf(data.length()))
                .body(data);

    }
    */


    //The Algorithm Loves You

}


