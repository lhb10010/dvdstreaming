package com.burkhead.dvdstreaming.repository;

import com.burkhead.dvdstreaming.model.MediaThumbnail;
import com.burkhead.dvdstreaming.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaThumbnailRepository extends JpaRepository<MediaThumbnail, Long> {



}
