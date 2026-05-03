package com.burkhead.dvdstreaming.repository;

import com.burkhead.dvdstreaming.model.Movie;
import com.burkhead.dvdstreaming.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {


    Video findVideoById(long id);
}
