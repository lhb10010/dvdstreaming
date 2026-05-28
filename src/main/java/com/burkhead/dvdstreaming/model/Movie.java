package com.burkhead.dvdstreaming.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.io.IOException;
import java.util.Base64;

import static java.lang.Double.NaN;

@Entity
public class Movie implements Media {

    //members

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;

    private long lastTimeWatched;

    private long lastTimeWatchedPos;

    private String genre;

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER)
    private Video movieVideo;

    @JsonIgnore
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] thumbnail;


    // ----------------------------- Constructors -------------------------------

    public Movie(){

    }

    public Movie(String title, String genre, byte[] image, Video video){

        this.title = title;
        this.genre = genre;
        this.thumbnail = image;
        this.movieVideo = video;
        this.lastTimeWatchedPos = 0;
        this.lastTimeWatched = -1;

    }


    //used by csv import
    public Movie(String title, String genre, long lastTimeWatched, long lastTimeWatchedPos, byte[] thumbnail, Video vid) throws IOException {
        this.title = title;
        this.genre = genre;
        this.lastTimeWatched = lastTimeWatched;
        this.lastTimeWatchedPos = 0;
        this.movieVideo = vid;
        this.thumbnail = thumbnail;

    }


    // ------------------------------------------- getters -------------------------------------------


    public long getId() {
        return id;
    }

    public String getTitle(){
        return this.title;
    }

    public long getLastTimeWatched(){
        return this.lastTimeWatched;
    }

    public long getLastTimeWatchedPos(){
        return this.lastTimeWatchedPos;
    }

    public String getGenre(){
        return this.genre;
    }

    @JsonIgnore
    public byte[] getThumbnail(){
        return this.thumbnail;
    }


    @JsonGetter("movieVideo")
    public Video getMovieVideo(){
        return this.movieVideo;
    }

    @Override
    public String getType() {
        return "movie";
    }

    @Override
    @JsonGetter("progress")
    public long getProgress(){
        return this.lastTimeWatchedPos;
    }

    @Override
    @JsonGetter("progressPercent")
    public double getProgressPercent(){

        if(this.movieVideo == null){
            return 0.0D;
        }

        long duration = this.movieVideo.getDurationMillis() / 1000;
        double rtrnVal = (double) this.lastTimeWatchedPos / (double) duration;

        if(Double.isNaN(rtrnVal)){
            return 0.0D;
        }

        return rtrnVal * 100;
    }


    // ------------------------------------------- setters -------------------------------------------


    public void setGenre(String genre){
        this.genre = genre;
    }

    public void setLastTimeWatchedPos(long timeWatched){

        if(this.movieVideo == null){
            this.lastTimeWatchedPos = 0;
            return;
        }

        long bottomCutoff = (long) (((double) this.movieVideo.getDurationMillis() / 1000) * 0.1);
        long topCutoff = (long) (((double) this.movieVideo.getDurationMillis() / 1000) * 0.9);

        if(timeWatched > 0 && timeWatched > bottomCutoff && timeWatched < topCutoff){
            this.lastTimeWatchedPos = timeWatched;
        }
        else{
            this.lastTimeWatchedPos = 0;
        }

        System.out.println("progress set: " + this.lastTimeWatchedPos + " timeWatched: " + timeWatched);
    }


}
