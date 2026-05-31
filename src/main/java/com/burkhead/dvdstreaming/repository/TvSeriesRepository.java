package com.burkhead.dvdstreaming.repository;

import com.burkhead.dvdstreaming.model.Movie;
import com.burkhead.dvdstreaming.model.TvEpisode;
import com.burkhead.dvdstreaming.model.TvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TvSeriesRepository extends JpaRepository<TvSeries, Long> {

    List<TvSeries> findByTitleContaining(String title);

    List<TvSeries> findAll();
}
