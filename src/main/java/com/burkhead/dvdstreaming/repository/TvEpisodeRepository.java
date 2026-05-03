package com.burkhead.dvdstreaming.repository;

import com.burkhead.dvdstreaming.model.TvEpisode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TvEpisodeRepository extends JpaRepository<TvEpisode, Long> {



}
