package com.burkhead.dvdstreaming.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class TvSeason {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int seasonNumber;

    @OneToMany
    @Nullable
    private List<TvEpisode> episodes;

    @ManyToOne
    private TvSeries series;

    //constructor

    public TvSeason(){

    }

    public TvSeason(int seasonNumber, TvSeries series){
        this.seasonNumber = seasonNumber;
        this.series = series;
        this.episodes = new ArrayList<TvEpisode>();
    }

    // getters

    public TvSeries getSeries(){
        return this.series;
    }

    //other

    public void addEpisode(TvEpisode ep){
        this.episodes.add(ep);
    }

    public long getId() {
        return this.id;
    }
}
