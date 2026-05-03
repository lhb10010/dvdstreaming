package com.burkhead.dvdstreaming.repository;

import com.burkhead.dvdstreaming.model.TvEpisode;
import com.burkhead.dvdstreaming.model.TvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TvSeriesRepository extends JpaRepository<TvSeries, Long> {



}
