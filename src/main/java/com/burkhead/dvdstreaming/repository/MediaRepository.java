package com.burkhead.dvdstreaming.repository;

import com.burkhead.dvdstreaming.model.Media;
import com.burkhead.dvdstreaming.model.Movie;
import com.burkhead.dvdstreaming.model.TvSeries;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;


public class MediaRepository {


    public final TvSeriesRepository tvSeriesRepository;
    public final MovieRepository movieRepository;

    public MediaRepository(TvSeriesRepository tvSeriesRepository, MovieRepository movieRepository) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.movieRepository = movieRepository;
    }


    public ArrayList<Media> findByTitleContaining(String title){
        ArrayList<Media> finds = new ArrayList<>(this.movieRepository.findByTitleContaining(title));
        finds.addAll(this.tvSeriesRepository.findByTitleContaining(title));
        return finds;
    }


}

