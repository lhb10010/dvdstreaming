package com.burkhead.dvdstreaming.repository;

import com.burkhead.dvdstreaming.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {


    Movie findMovieById(long id);

    List<Movie> findByTitleContaining(String title);
}
