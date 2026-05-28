package com.burkhead.dvdstreaming.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class TvSeries implements Media {

    //members

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;

    private long lastTimeWatched;

    @OneToOne
    @Nullable
    private TvEpisode lastEpisodeWatched;

    private long lastEpisodeWatchedPos;

    @OneToMany
    @Nullable
    private List<TvSeason> seasons;

    private String genre;

    //thumbnail
    private byte[] thumbnail;

    //constructor

    private void genericNew(){
        this.lastTimeWatched = -1;
        this.lastEpisodeWatched = null;
        this.lastEpisodeWatchedPos = 0;
        this.seasons = new ArrayList<>();
    }

    public TvSeries(){

    }

    //used for CSV import
    public TvSeries(String title, String genre, long lastTimeWatched, String thumbnailPath){
        this.title = title;
        this.genre = genre;
        this.lastTimeWatched = lastTimeWatched;
        this.lastEpisodeWatched = null;
        this.lastEpisodeWatchedPos = 0;
        this.seasons = new ArrayList<>();
    }

    //getters

    public String getTitle(){
        return this.title;
    }

    public long getLastTimeWatched(){
        return this.lastTimeWatched;
    }

    @Override
    public long getId() {
        return this.id;
    }

    public String getGenres(){
        return this.genre;
    }

    @Override
    public String getType() {
        return "series";
    }


    public long getProgress(){
        return this.lastEpisodeWatchedPos;
    }


    public double getProgressPercent(){
        return 0.0; //TODO make real
    }
    //setters


}
