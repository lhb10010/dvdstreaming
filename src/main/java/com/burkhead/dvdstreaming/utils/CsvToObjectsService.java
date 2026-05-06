package com.burkhead.dvdstreaming.utils;

import com.burkhead.dvdstreaming.model.*;
import com.burkhead.dvdstreaming.repository.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;


@Service
public class CsvToObjectsService {

    //members
    private final VideoRepository videoRepository;

    private final TvSeriesRepository tvSeriesRepository;

    private final TvSeasonRepository tvSeasonRepository;

    private final TvEpisodeRepository tvEpisodeRepository;

    private final MovieRepository movieRepository;


    public ArrayList<Video> csvToVideoObjects(String csvPath){

        File csv = new File(csvPath);
        ArrayList<Video> videoArray = new ArrayList<Video>();

        try (Scanner s = new Scanner(csv)){

            while(s.hasNextLine()) {
                String line = s.nextLine();
                String[] values = line.split(",");
                Video v = new Video(values[0], Long.parseLong(values[1]));
                videoArray.add(v);
            }
        }
        catch (FileNotFoundException e){
            System.out.println("file not found");
            return new ArrayList<Video>();
        }
        return videoArray;
    }

    public ArrayList<Movie> csvToMovieObjects(String csvPath){

        File csv = new File(csvPath);
        ArrayList<Movie> movieArray = new ArrayList<Movie>();

        try (Scanner s = new Scanner(csv)){

            while(s.hasNextLine()) {
                String line = s.nextLine();
                String[] values = line.split(",");

                byte[] imageData = new byte[0];
                try {
                    System.out.println(values[4]);
                    imageData = Files.readAllBytes(Path.of(values[4]));
                }
                catch(IOException e){

                }

                Movie v = new Movie(values[0], values[1], Long.parseLong(values[2]), Long.parseLong(values[3]), imageData, videoRepository.getReferenceById(Long.parseLong(values[5])));
                movieArray.add(v);
            }
        }
        catch (IOException e){
            System.out.println("file not found");
            return new ArrayList<Movie>();
        }
        return movieArray;
    }

    public ArrayList<TvEpisode> csvToEpisodeObjects(String csvPath){

        File csv = new File(csvPath);
        ArrayList<TvEpisode> episodeArray = new ArrayList<TvEpisode>();

        try (Scanner s = new Scanner(csv)){

            while(s.hasNextLine()) {
                String line = s.nextLine();
                String[] values = line.split(",");
                TvEpisode v = new TvEpisode(values[0], Integer.parseInt(values[1]), videoRepository.getReferenceById(Long.parseLong(values[2])), tvSeasonRepository.getReferenceById(Long.parseLong(values[3])));
                episodeArray.add(v);
            }
        }
        catch (FileNotFoundException e){
            System.out.println("file not found");
            return new ArrayList<TvEpisode>();
        }
        return episodeArray;
    }

    public ArrayList<TvSeries> csvTvSeriesObjects(String csvPath){

        File csv = new File(csvPath);
        ArrayList<TvSeries> seriesArray = new ArrayList<TvSeries>();

        try (Scanner s = new Scanner(csv)){

            while(s.hasNextLine()) {
                String line = s.nextLine();
                String[] values = line.split(",");
                TvSeries v = new TvSeries(values[0], values[1], Long.parseLong(values[2]), values[3]);
                seriesArray.add(v);
            }
        }
        catch (FileNotFoundException e){
            System.out.println("file not found");
            return new ArrayList<TvSeries>();
        }
        return seriesArray;
    }

    public ArrayList<TvSeason> csvTvSeasonObjects(String csvPath){

        File csv = new File(csvPath);
        ArrayList<TvSeason> seasonArray = new ArrayList<TvSeason>();

        try (Scanner s = new Scanner(csv)){

            while(s.hasNextLine()) {
                String line = s.nextLine();
                String[] values = line.split(",");
                TvSeason v = new TvSeason(Integer.parseInt(values[0]), tvSeriesRepository.getReferenceById(Long.parseLong(values[1])));
                seasonArray.add(v);
            }
        }
        catch (FileNotFoundException e){
            System.out.println("file not found");
            return new ArrayList<TvSeason>();
        }
        return seasonArray;
    }

    //constructor

    public CsvToObjectsService(VideoRepository videoRepository, TvSeriesRepository tvSeriesRepository, TvSeasonRepository tvSeasonRepository, TvEpisodeRepository tvEpisodeRepository, MovieRepository movieRepository){
        this.videoRepository = videoRepository;
        this.tvSeriesRepository = tvSeriesRepository;
        this.tvSeasonRepository = tvSeasonRepository;
        this.tvEpisodeRepository = tvEpisodeRepository;
        this.movieRepository = movieRepository;
    }

    //on start

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void injectDatabaseTestData(){

        videoRepository.saveAll(csvToVideoObjects("src/main/resources/dummyData/dataCsvs/video.csv"));
        movieRepository.saveAll(csvToMovieObjects("src/main/resources/dummyData/dataCsvs/movie.csv"));
        tvSeriesRepository.saveAll(csvTvSeriesObjects("src/main/resources/dummyData/dataCsvs/series.csv"));
        tvSeasonRepository.saveAll(csvTvSeasonObjects("src/main/resources/dummyData/dataCsvs/season.csv"));
        tvEpisodeRepository.saveAll(csvToEpisodeObjects("src/main/resources/dummyData/dataCsvs/episode.csv"));

    }

    //userRepository.saveAll(users);

}
