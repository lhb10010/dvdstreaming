package com.burkhead.dvdstreaming.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.io.IOException;

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

    }

    //used by csv import
    public Movie(String title, String genre, long lastTimeWatched, long lastTimeWatchedPos, byte[] thumbnail, Video vid) throws IOException {
        this.title = title;
        this.genre = genre;
        this.lastTimeWatched = lastTimeWatched;
        this.lastTimeWatchedPos = lastTimeWatchedPos;
        this.movieVideo = vid;
        this.thumbnail = thumbnail;

    }

    //getters

    public Long getId() {
        return id;
    }

    public String getTitle(){
        return this.title;
    }

    public long getLastTimeWatched(){
        return this.lastTimeWatched;
    }

    public String getGenre(){
        return this.genre;
    }

    @JsonIgnore
    public byte[] getThumbnail(){
        return this.thumbnail;
    }

    @JsonGetter("video")
    public long getVideoId(){
        return this.movieVideo.getId();
    }

    //setters

    public void setGenre(String genre){
        this.genre = genre;
    }

}
