package com.burkhead.dvdstreaming.model;

//temporary object to hold information while a new video upload is processing

import com.burkhead.dvdstreaming.repository.ProcessingVideoRepository;
import com.burkhead.dvdstreaming.repository.VideoRepository;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
    private int fragTime = 2000; //TODO make config
    private double ffmpegProcessingPercentage;
    @Nullable
    @OneToOne
    private Video v;


    // ------------------------------------- Construscots -------------------------------------


    public ProcessingVideo(long chunkCount, long totalByteCount){
        this.chunkCount = chunkCount;
        this.totalByteCount = totalByteCount;
        this.chunkFolderPath = "";
        this.wholeVideoPath = "";
        this.finalVideoPath = "";
        this.v = null;
    }

    public ProcessingVideo(){
        this.v = null;
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
    public String convertToFinalFMP4(ProcessingVideoRepository p, VideoRepository v){

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
        if(!runFFMPEGCommand(this.wholeVideoPath, finalVideoPath, p, v)){
            System.out.println("finalFMP4 error 3");
            return "";
        }

        this.finalVideoPath = finalVideoPath;
        return finalVideoPath;

    }

    // -------------------------------- Helpers ----------------------------------


    private boolean runFFMPEGCommand(String inputVidPath, String outPath, ProcessingVideoRepository p, VideoRepository v){

        System.out.println("Strating ffmpeg");
        long durationSeconds = calcDurationOfMp4(inputVidPath) / 1000;

        //ffmpeg -i input.mp4 -c:v libx264 -c:a aac -g 60 -keyint_min 60 -sc_threshold 0 -movflags frag_keyframe+dash+delay_moov+global_sidx+default_base_moof x264opts "keyint=48:min-keyint=48:no-scenecut" -f mp4 output.mp4
        //ffmpeg -i input.mp4 -c:v libx264 -c:a aac -g 60 -keyint_min 60 -sc_threshold 0 -movflags frag_keyframe+dash+delay_moov+global_sidx+default_base_moof -x264opts "keyint=48:min-keyint=48:no-scenecut" -f mp4 output.mp4

        String command = "ffmpeg -i " + inputVidPath +" -c:v libx264 -c:a aac -g 60 -keyint_min 60 -sc_threshold 0 -movflags " +
                "frag_keyframe+empty_moov+default_base_moof+global_sidx+omit_tfhd_offset -x264opts " +
                "\"keyint=60:min-keyint=60:no-scenecut\" -frag_duration 2000000 -f mp4 " + outPath; //TODO make config


        try {

            ProcessBuilder ffmpegProcess = new ProcessBuilder("sh", "-c", command);

            //monitor stdout
            Thread monitorProgress = new Thread(() -> {
                try {
                    monitorStdOut(ffmpegProcess.start(), durationSeconds, p, v);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            monitorProgress.start();

        }
        catch (Exception e){
            return false;
        }

        return true;
    }


    private void monitorStdOut(Process ffmpeg, long durationSeconds, ProcessingVideoRepository p, VideoRepository v){

        long fps = 30; //TODO CONFIG
        long frames = fps * durationSeconds;

        System.out.println("totalFrames: " + frames);

        try{

            //setup
            InputStream is = ffmpeg.getErrorStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);


            String line = br.readLine();
            System.out.println(line);
            while(line != null){
                //System.out.println("here5");
                System.out.println(line);
                if(line.contains("frame=")){
                    line = line.split(" fps")[0].replace("frame=", "").replace(" ", "");
                    System.out.println("num: " + line);
                    double percent = (Double.parseDouble(line) / frames) * 100;
                    if(percent > 99){
                        percent = 99.0D;
                    }
                    System.out.println(percent);
                    this.ffmpegProcessingPercentage = percent;
                    p.save(this);
                }
                line = br.readLine();
            }

            System.out.println("here6");
            this.ffmpegProcessingPercentage = 100.0D;

            assert this.v != null;
            this.v.completeVideoFromProcessingVideo(v);
            this.cleanUpThis(p);

        }
        catch (Exception e){
            this.ffmpegProcessingPercentage = -1D;
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


    // -------------------------------- cleanup ----------------------------------


    private void cleanUpThis(ProcessingVideoRepository p){ //TODO not void
        cleanUpChunkFiles();
        cleanUpRemainingFilesAndDirectory();
        p.delete(this);
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

    public double getFfmpegProcessingPercentage(){return this.ffmpegProcessingPercentage;}


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

    public void setVideo(Video v){
        this.v = v;
    }
}
