package com.burkhead.dvdstreaming.repository;

import com.burkhead.dvdstreaming.model.ProcessingVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessingVideoRepository extends JpaRepository<ProcessingVideo, Long> {

    ProcessingVideo findProcessingVideoById(long id);
}
