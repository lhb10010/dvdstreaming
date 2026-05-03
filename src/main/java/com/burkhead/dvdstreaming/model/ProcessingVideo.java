package com.burkhead.dvdstreaming.model;

//temporary object to hold information while a new video upload is processing

import com.burkhead.dvdstreaming.repository.ProcessingVideoRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

@Entity
public class ProcessingVideo {

    private static final String CHUNK_VIDEO_PARENT_FOLDER = "src/main/resources/chunkedUploads/"; //TODO make config

    // ------------------------------------- member variables -------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String chunkFolderPath;
    private String wholeVideoPath;
    private String finalVideoPath;
    private long chunkCount;
    private long totalByteCount;
    private int fragTime = 2000;


    // ------------------------------------- Construscots -------------------------------------


    public ProcessingVideo(long chunkCount, long totalByteCount){
        this.chunkCount = chunkCount;
        this.totalByteCount = totalByteCount;
        this.chunkFolderPath = "";
        this.wholeVideoPath = "";
        this.finalVideoPath = "";
    }

    public ProcessingVideo(){

    }


    // -------------------------------- chunk folder interactions ----------------------------------


    //create the folder where chunks will be saved
    //step 1
    public boolean createChunkFolder(){

        //check if folder already created
        if(!this.chunkFolderPath.isEmpty()){
            System.out.println("createChunkFolder error 1");
            return false;
        }

        //create path
        this.chunkFolderPath = CHUNK_VIDEO_PARENT_FOLDER + this.id;

        //create folder and ensure a folder with the same name doesnt exist
        File f = new File(this.chunkFolderPath);
        if(f.exists() || !f.mkdir()) {
            System.out.println("createChunkFolder error 2");
            return false;
        }
        return true;

    }


    //create a new .part file for a chunk
    //step 2
    public boolean addChunkToFolder(byte[] chunk, long chunkNum){

        //check that folder was created
        if(this.chunkFolderPath.isEmpty()){
            System.out.println("addChunk error 1");
            return false;
        }

        //check that folder still exists
        File dir = new File(this.chunkFolderPath);
        if(!dir.exists()){
            System.out.println("addChunk error 2");
            return false;
        }

        //try to create part filer
        try{

            //check if file already exists and create it
            String thisFilePath = this.chunkFolderPath + "/" + chunkNum + ".part";
            File f = new File(thisFilePath);
            if(f.exists() || !f.createNewFile()){
                System.out.println("addChunk error 3");
                return false;
            }

            //write to file
            Path p = Paths.get(f.getPath());
            Files.write(p, chunk);

        }
        catch (IOException e){
            System.out.println("addChunk error 4");
            return false;
        }
        return true;
    }


    public ArrayList<Long> checkForMissingChunks(){

        ArrayList<Long> missingChunks = new ArrayList<Long>();
        for(long i = 0; i < this.chunkCount; i++){
            String path = this.chunkFolderPath + "/" + i + ".part";
            File f = new File(path);
            if(!f.exists()){
                missingChunks.add(i);
            }
        }

        return missingChunks;
    }


    //reconstruct file from parts
    //step 3
    public String reconstructVideoFile(){

        String completeVideoPath = this.chunkFolderPath + "/whole.mp4";

        try { //TODO close stream on Error

            //create new file that will be the reconstructed file
            File f = new File(completeVideoPath);
            if(f.exists() || !f.createNewFile()){
                System.out.println("reconstructVideoFile error 1");
                return "";
            }

            //write all chunks to a single file
            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f, true))) {
                for (long i = 0; i < chunkCount; i++) {
                    Path p = Paths.get(this.chunkFolderPath + "/" + i + ".part");
                    byte[] data = Files.readAllBytes(p);
                    out.write(data);
                    Files.delete(p);
                }
            }


        }
        catch(IOException e){
            System.out.println("reconstructVideoFile error 2");
            return "";
        }

        this.wholeVideoPath = completeVideoPath;
        return completeVideoPath;

    }


    //step 4
    public String convertToFinalFMP4(){

        if(this.wholeVideoPath.isEmpty()){
            System.out.println("finalFMP4 error 1");
            return "";
        }

        File whole = new File(this.wholeVideoPath);
        if(!whole.exists()){
            System.out.println("finalFMP4 error 2");
            return "";
        }

        String finalVideoPath = this.chunkFolderPath + "/final.mp4";
        if(!runFFMPEGCommand(this.wholeVideoPath, this.fragTime, finalVideoPath)){
            System.out.println("finalFMP4 error 3");
            return "";
        }

        this.finalVideoPath = finalVideoPath;
        return finalVideoPath;

    }

    // -------------------------------- Helpers ----------------------------------


    private boolean runFFMPEGCommand(String inputVidPath, int fragLenMilliseconds, String outPath){

        System.out.println("Strating ffmpeg");

        String command = "ffmpeg -i " + inputVidPath + " -c copy -map 0 -movflags " +
                "+frag_keyframe+empty_moov+default_base_moof -frag_duration " + fragLenMilliseconds +
                "000 -f mp4 " + outPath; //TODO make config


        try {

            ProcessBuilder ffmpegProcess = new ProcessBuilder("sh", "-c", command);
            Process p = ffmpegProcess.start();
            while(p.isAlive()){

            }
            if(p.exitValue() != 0){
                return false;
            }

        }
        catch (Exception e){
            return false;
        }

        System.out.println("ffmpeg done");
        return true;
    }


    // -------------------------------- cleanup ----------------------------------


    public void cleanUpThis(ProcessingVideoRepository p){ //TODO not void
        //cleanUpChunkFiles();
        //cleanUpRemainingFilesAndDirectory();
        //p.delete(this);
    }

    private void cleanUpChunkFiles(){

        File dir = new File(this.chunkFolderPath);
        for(File f : Objects.requireNonNull(dir.listFiles())){
            if(f.getPath().endsWith(".part"))
                f.delete();
        }
    }

    private void cleanUpRemainingFilesAndDirectory(){

        File dir = new File(this.chunkFolderPath);
        for(File f : Objects.requireNonNull(dir.listFiles())){
            f.delete();
        }
        dir.delete();

    }


    // -------------------------------- getters ----------------------------------


    public Long getId() {
        return id;
    }

    public String getFinalVideoPath() {
        return finalVideoPath;
    }

    public int getFragTime() {
        return fragTime;
    }

    public long getChunkCount() {
        return chunkCount;
    }

    public long getTotalByteCount() {
        return totalByteCount;
    }

    public String getChunkFolderPath() {
        return chunkFolderPath;
    }

    public String getWholeVideoPath() {
        return wholeVideoPath;
    }


    // -------------------------------- setters ----------------------------------



    public void setId(Long id) {
        this.id = id;
    }

    public void setChunkCount(long chunkCount) {
        this.chunkCount = chunkCount;
    }

    public void setFinalVideoPath(String finalVideoPath) {
        this.finalVideoPath = finalVideoPath;
    }

    public void setFragTime(int fragTime) {
        this.fragTime = fragTime;
    }

    public void setChunkFolderPath(String chunkFolderPath) {
        this.chunkFolderPath = chunkFolderPath;
    }

    public void setTotalByteCount(long totalByteCount) {
        this.totalByteCount = totalByteCount;
    }

    public void setWholeVideoPath(String wholeVideoPath) {
        this.wholeVideoPath = wholeVideoPath;
    }
}
