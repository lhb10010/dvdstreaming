package com.burkhead.dvdstreaming.model;


import com.burkhead.dvdstreaming.repository.VideoRepository;
import com.burkhead.dvdstreaming.utils.ConfigValues;
import com.burkhead.dvdstreaming.utils.Mp4Parser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;

@Entity
public class Video {

    // ------------------------------------------ Fields -----------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String videoFolderPath;

    private long bytesLength;

    private long timeLength;

    private int fragLength; //fragment length in milliseconds


    // ------------------------------------------ Constructors -----------------------------------------------

    public static Video createVideoFromProcesingVideo(ProcessingVideo p, VideoRepository videoRepository){

        System.out.println("Creating Video");
        Video v = new Video();
        videoRepository.save(v);

        System.out.println(v.id);
        v.fragLength = p.getFragTime();
        v.createVideoDir();

        File f = new File(p.getFinalVideoPath());
        if(f.exists()){
            v.bytesLength = f.length();
        }

        Mp4Parser.parse(p.getFinalVideoPath(), v.videoFolderPath);

        videoRepository.save(v);

        System.out.println("Done Creating Video");

        return v;

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


    }

    public Video(String path, long byteLen, long timeLen){
        this.videoFolderPath = path;
        this.bytesLength = byteLen;
        this.timeLength = timeLen;
    }


    // ------------------------------------------ Getters -----------------------------------------------


    public long getBytesLength() {
        return bytesLength;
    }

    public Long getId() {
        return id;
    }


    //get ftyp and moov
    @JsonIgnore
    public byte[] getInitData(){

        try {
            Path initFilePath = Paths.get("src/main/resources/videoFiles/" + this.id + "/" + this.id + ".init");
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

    // ------------------------------------------ ToString Override -----------------------------------------------


    @Override
    public String toString() {
        return String.valueOf(this.id);
    }


    //TODO make real
    private static long tempId = 3;
    public static long getNextId(){
        long returnId = tempId;
        tempId++;
        return returnId;
    }


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


    long getVideoTimeLength(String vidPath){

        //TODO make real
        return 1000000;

    }

}
