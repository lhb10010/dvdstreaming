package com.burkhead.dvdstreaming.model;


import com.burkhead.dvdstreaming.repository.VideoRepository;
import com.burkhead.dvdstreaming.utils.ConfigValues;
import com.burkhead.dvdstreaming.utils.Mp4Parser;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;

@Entity
public class Video {

    // ------------------------------------------ Fields -----------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Nullable
    private String videoFolderPath;

    private long bytesLength;

    private int fragLength; //fragment length in milliseconds

    private long durationMillis;

    @OneToOne
    @Nullable
    private ProcessingVideo p;


    // ------------------------------------------ Constructors -----------------------------------------------

    public void completeVideoFromProcessingVideo(VideoRepository videoRepository){

        System.out.println("Creating Video");

        this.fragLength = p.getFragTime();
        this.createVideoDir();

        this.durationMillis = this.calcDurationOfMp4(p.getFinalVideoPath());

        File f = new File(p.getFinalVideoPath());
        if(f.exists()){
            this.bytesLength = f.length();
        }

        Mp4Parser.parse(p.getFinalVideoPath(), this.videoFolderPath);

        this.p = null;
        videoRepository.save(this);

        System.out.println("Done Creating Video");

    }


    //probably no longer needed
    public static Video createVideoFromProcesingVideo(ProcessingVideo p, VideoRepository videoRepository){

        System.out.println("Creating Video");
        Video v = new Video();
        videoRepository.save(v);

        System.out.println(v.id);
        v.fragLength = p.getFragTime();
        v.createVideoDir();

        v.durationMillis = v.calcDurationOfMp4(p.getFinalVideoPath());

        File f = new File(p.getFinalVideoPath());
        if(f.exists()){
            v.bytesLength = f.length();
        }

        Mp4Parser.parse(p.getFinalVideoPath(), v.videoFolderPath);

        videoRepository.save(v);

        System.out.println("Done Creating Video");

        return v;

    }


    public Video(ProcessingVideo p){
        this.p = p;
        this.videoFolderPath = null;
        this.durationMillis = -1;
        this.bytesLength = -1;
        this.fragLength = -1;
        this.p.setVideo(this);
    }


    private Video(){


    }


/*

    //TODO make videoMaker object to do this
    public Video(long vidId, String filePath){

        //setup vars
        this.fragLength = 2000;
        this.id = vidId;

        //create the permenent folder for the video to be stored in
        createVideoDir();

        //make properly formatted FMP4 with FFMPEG
        String tempWholeVideoPath = this.videoFolderPath + "whole.mp4";
        runFFMPEGCommand(filePath, this.fragLength, tempWholeVideoPath);

        //get out file size
        File f = new File(tempWholeVideoPath);
        if(f.exists()){
            this.bytesLength = f.length();
        }
        else{
            this.errorState = true;
            return;
        }

        //get time length in seconds
        this.timeLength = getVideoTimeLength(tempWholeVideoPath);

        Mp4Parser.parse(filePath, vidId);

    }
*/


    /*
    @Transactional
public User createUser(String name) {
    User user = new User(name);
    user = userRepository.save(user);

    // Now you have the ID
    performLogicWithId(user.getId());
    return user;
}
     */

    //remove id if possible. used by csv import. loads entire movie into memory
    public Video(String ogPath, long id){

        //temp
        try {
            if (id == 0) {
                Files.walk(Path.of("src/main/resources/videoFiles/" + id))
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
            //make overall videos directory
            File movieDir = new File("src/main/resources/videoFiles");
            if(!movieDir.exists()){
                movieDir.mkdir();
            }
            File movieIndDir = new File("src/main/resources/videoFiles/" + id);
            if(!movieIndDir.exists()){
                movieIndDir.mkdir();
            }
        }
        catch (IOException e){

        }


        try {

            //get file size
            Path p = Paths.get(ogPath);
            this.bytesLength = Files.readAttributes(p, BasicFileAttributes.class).size();
            byte[] allData = Files.readAllBytes(p);
            Mp4Parser.parse(ogPath, "src/main/resources/videoFiles/" + id);

        }
        catch (IOException e){
            //TODO error
        }
        System.out.println("\n\n\n\n\n\n\n\n");

    }

    public Video(String path, long byteLen, long timeLen){
        this.videoFolderPath = path;
        this.bytesLength = byteLen;
    }


    // ------------------------------------------ Getters -----------------------------------------------

    public ProcessingVideo getProcessingVideo(){
        return this.p;
    }

    public long getBytesLength() {
        return bytesLength;
    }

    public Long getId() {
        return id;
    }

    public Long getDurationMillis(){
        return this.durationMillis;
    }


    //get ftyp and moov
    @JsonIgnore
    public byte[] getInitData(){

        try {
            Path initFilePath = Paths.get("src/main/resources/videoFiles/" + this.id + "/ftypmoov.init");
            return Files.readAllBytes(initFilePath);
        }
        catch(IOException e){
            //TODO error
        }

        return new byte[0];
    }

    //get moov + mdat fragment
    @JsonIgnore
    public byte[] getFragment(int fragNum){

        try {
            Path fragFilePath = Paths.get("src/main/resources/videoFiles/" + this.id + "/" + fragNum + ".frag");
            return Files.readAllBytes(fragFilePath);
        }
        catch(IOException e){
            //TODO error
        }

        return new byte[0];
    }


    //sdixs
    @JsonIgnore
    public byte[] getMapData(){

        try {
            Path initFilePath = Paths.get("src/main/resources/videoFiles/" + this.id + "/map.init");
            return Files.readAllBytes(initFilePath);
        }
        catch(IOException e){
            //TODO error
        }

        return new byte[0];
    }


    @JsonGetter("isReady")
    public boolean isReady(){
        return p == null;
    }


    public void setProcessingVideoNull(){
        this.p = null;
    }


    // ------------------------------------------ ToString Override -----------------------------------------------

    /*
    @Override
    public String toString() {
        return String.valueOf(this.id);
    }

     */


    // ------------------------------------------ Helper Functions -----------------------------------------------


    private void createVideoDir(){

        String videoDirPath = ConfigValues.getValueAsPath("permanentVideoStoreDir") + this.id;
        File newDir = new File(videoDirPath);
        if(!newDir.mkdir()){
            return; //TODO Error
        }
        else{
            this.videoFolderPath = videoDirPath + "/";
        }

    }



    private long calcDurationOfMp4(String path){

        String command = "ffmpeg -i " + path + " 2>&1 | grep Duration: | awk '{print $2}'";

        try {

            ProcessBuilder ffmpegProcess = new ProcessBuilder("sh", "-c", command);
            Process p = ffmpegProcess.start();
            while(p.isAlive()){

            }
            if(p.exitValue() != 0){
                System.out.println("returned " + p.exitValue());
                return -1;
            }

            long millis = 0L;
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            out = out.replace(",", "");
            System.out.println(out);
            String[] split1 = out.split("\\.");
            if(split1.length != 2)
                return -1;
            String[] split2 = split1[0].split(":");
            if(split2.length != 3)
                return -1;
            
            millis += (60 * 60 * 1000 * Long.parseLong(split2[0]));
            millis += (60 * 1000 * Long.parseLong(split2[1]));
            millis += (1000 * Long.parseLong(split2[2]));
            millis += Long.parseLong(split1[1].replace("\n", ""));
            
            return millis;

        }
        catch (Exception e){
            System.out.println("calc err 1");
            System.out.println(e.fillInStackTrace());
            return -1;
        }

    }

}
