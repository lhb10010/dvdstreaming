package com.burkhead.dvdstreaming.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

@Entity
public class TvEpisode implements VideoContainer {

    //members

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String episodeTitle;

    private int episodeNum;

    @ManyToOne
    private TvSeason season;

    @Nullable
    @OneToOne
    private Video episodeVideo;

    //thumbnail

    //constructor

    public TvEpisode(){

    }

    public TvEpisode(String title, int number, TvSeason season){
        this.episodeTitle = title;
        this.episodeNum = number;
        this.episodeVideo = null;
        this.season = season;
    }

    //getters

    public TvSeries getSeries() {
        return this.season.getSeries();
    }

    public long getId() {
        return this.id;
    }

    @Override
    public void setVideo(Video v) {
        this.episodeVideo = v;
    }
}
