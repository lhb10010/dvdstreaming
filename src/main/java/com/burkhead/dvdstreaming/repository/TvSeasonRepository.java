package com.burkhead.dvdstreaming.repository;

import com.burkhead.dvdstreaming.model.TvSeason;
import com.burkhead.dvdstreaming.model.TvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TvSeasonRepository extends JpaRepository<TvSeason, Long> {
    TvSeason findTvSeasonById(long id);


}
