package com.burkhead.dvdstreaming.model;

import jakarta.persistence.*;

@Entity
public class TvEpisode {

    //members

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String episodeTitle;

    private int episodeNum;

    @ManyToOne
    private TvSeason season;

    @OneToOne
    private Video episodeVideo;

    //thumbnail

    //constructor

    public TvEpisode(){

    }

    //used by csv import
    public TvEpisode(String title, int episodeNum, Video vid, TvSeason season){
        this.episodeTitle = title;
        this.episodeNum = episodeNum;
        this.episodeVideo = vid;
        this.season = season;
        this.season.addEpisode(this);
    }

    //getters

    public TvSeries getSeries() {
        return this.season.getSeries();
    }

}
