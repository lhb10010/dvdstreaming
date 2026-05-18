package com.burkhead.dvdstreaming.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.exit;


//strip all other data: ffmpeg -i videoplayback.mp4 -map_metadata -1 -c:v copy -c:a copy -sn output.mp4
//fragmented with set intervols (2 seconds): ffmpeg -i input.mp4 -c copy -map 0 -movflags +frag_keyframe+empty_moov+default_base_moof -frag_duration 2000000 -f mp4 output.mp4
//-i : read from stdin


//TODO all fragments being held in memory at the same time: fix this


public class Mp4Parser {


    public static boolean parse(String ogPath, String outFolderPath){

        System.out.println(ogPath);
        System.out.println(outFolderPath);

        //get data from file
        byte[] initData = new byte[0];
        byte[] mapData = new byte[0];
        ArrayList<byte[]> frags = new ArrayList<>();
        try {
            File f = new File(ogPath);
            FileInputStream inp = new FileInputStream(f);

            //parse init data
            initData = getInitData(inp);

            //parse sdix data
            mapData = getMapData(inp);

            //parse fragments
            frags = getAllFragments(inp);
        }
        catch (Exception e){
            System.out.println("parseError 1");
            return false;
        }

        System.out.println(frags.size());


        //save init file
        try{
            String filePath = outFolderPath + "/ftypmoov.init";

            //create file
            File fragFile = new File(filePath);
            if (!fragFile.createNewFile()) {
                throw new IOException();
            }

            //write data to file
            Path fragFileWrite = Paths.get(filePath);
            Files.write(fragFileWrite, initData);
        }
        catch(IOException e){
            return false;
        }


        //save map file
        try{
            String filePath = outFolderPath + "/map.init";

            //create file
            File fragFile = new File(filePath);
            if (!fragFile.createNewFile()) {
                throw new IOException();
            }

            //write data to file
            Path fragFileWrite = Paths.get(filePath);
            Files.write(fragFileWrite, mapData);
        }
        catch(IOException e){
            return false;
        }


        //save fragments
        int i = 0;
        for(byte[] frag : frags){
            if(!writeFragFile(i, frag, outFolderPath)){
                System.out.println("parseError 2");
                return false;
            }
            i++;
        }

        return true;

    }

    private static byte[] getMapData(FileInputStream f) {
        byte[] sidx1 = parseInputStreamForTopLevelBox(f, "sidx");
        byte[] sidx2 = parseInputStreamForTopLevelBox(f, "sidx");

        return combineBytes(sidx1, sidx2);
    }


    private static byte[] getInitData(FileInputStream f){

        //get init boxes
        byte[] ftyp = parseInputStreamForTopLevelBox(f, "ftyp");
        byte[] moov = parseInputStreamForTopLevelBox(f, "moov");

        return combineBytes(ftyp, moov);

    }


    public static boolean writeFragFile(int num, byte[] data, String outFolderPath){
        try{
            String filePath = outFolderPath + num + ".frag";
            //System.out.println("frag: " + filePath);

            //create file
            File fragFile = new File(filePath);
            if (!fragFile.createNewFile()) {
                throw new IOException();
            }

            //write data to file
            Path fragFileWrite = Paths.get(filePath);
            Files.write(fragFileWrite, data);
        }
        catch(IOException e){
            return false;
        }

        return true;
    }


    public static ArrayList<byte[]> getAllFragments(FileInputStream f){

        ArrayList<byte[]> fragments = new ArrayList<>();
        byte[] currentMoof = new byte[0];
        //byte[] sidx1 = new byte[0];
        //byte[] sidx2 = new byte[0];

        try {

            while (f.available() > 0) {

                byte[] sizeBytes = new byte[4];
                System.arraycopy(f.readNBytes(4), 0, sizeBytes, 0, 4);
                int boxSize = ByteBuffer.wrap(sizeBytes).getInt();
                //System.out.println(boxSize);

                byte[] typeBytes = new byte[4];
                System.arraycopy(f.readNBytes(4), 0, typeBytes, 0, 4);
                String type = new String(typeBytes);
                //System.out.println(type);


                if (type.equals("moof")) {

                    //System.out.println("here1");
                    currentMoof = new byte[boxSize];
                    System.arraycopy(f.readNBytes(boxSize - 8), 0, currentMoof, 8, boxSize - 8);
                    System.arraycopy(sizeBytes, 0, currentMoof, 0, 4);
                    System.arraycopy(typeBytes, 0, currentMoof, 4, 4);

                } else if (type.equals("mdat") && currentMoof.length != 0) {

                    //System.out.println("here2");

                    byte[] currentMdat = new byte[boxSize];
                    System.arraycopy(f.readNBytes(boxSize - 8), 0, currentMdat, 8, boxSize - 8);
                    System.arraycopy(sizeBytes, 0, currentMdat, 0, 4);
                    System.arraycopy(typeBytes, 0, currentMdat, 4, 4);

                   // byte[] combined1 = combineBytes(sidx1, sidx2);
                    byte[] combined2 = combineBytes(currentMoof, currentMdat);
                    //byte[] combined = combineBytes(combined1, combined2);
                    fragments.add(combined2);
                    currentMoof = new byte[0];
                    //sidx1 = new byte[0];
                    //sidx2 = new byte[0];

                } else if (type.equals("mdat") && currentMoof.length == 0) {
                    //TODO: warning
                    //TODO: handle missing sidx too
                }else if(type.equals("mfra")){
                    break;
                }

            }
        } catch (Exception e) {
            System.out.println("fragerror 1");
            fragments = new ArrayList<>();
        }

        return fragments;

    }


    private static byte[] combineBytes(byte[] byte1, byte[] byte2){
        byte[] combined = new byte[byte2.length + byte1.length];
        System.arraycopy(byte1, 0, combined, 0, byte1.length);
        System.arraycopy(byte2, 0, combined, byte1.length, byte2.length);
        return combined;
    }


    public static byte[] parseBoxForSubBox(byte[] boxData, String target){

        byte[] subData = new byte[boxData.length - 8];
        System.arraycopy(boxData, 8, subData, 0, boxData.length - 8);
        return parseForDataForTopLevelBox(subData, target);

    }


    public static byte[] parseForDataForTopLevelBox(byte[] data, String target){


        int pos = 0;
        while(pos < data.length){

            byte[] sizeBytes = new byte[4];
            System.arraycopy(data, pos, sizeBytes, 0, 4);
            int boxSize = ByteBuffer.wrap(sizeBytes).getInt();

            byte[] typeBytes = new byte[4];
            System.arraycopy(data, pos + 4, typeBytes, 0, 4);
            String type = new String(typeBytes);
            //System.out.println(type);

            if(type.equals(target)){
                byte[] targetData = new byte[boxSize];
                System.arraycopy(data, pos, targetData, 0, boxSize);
                return targetData;
            }

            pos += boxSize;

        }

        return new byte[0];

    }


    public static byte[] parseInputStreamForTopLevelBox(FileInputStream f, String target){

        try {

            while (f.available() > 0) {

                byte[] sizeBytes = new byte[4];
                System.arraycopy(f.readNBytes(4), 0, sizeBytes, 0, 4);
                int boxSize = ByteBuffer.wrap(sizeBytes).getInt();
                //System.out.println(boxSize);

                byte[] typeBytes = new byte[4];
                System.arraycopy(f.readNBytes(4), 0, typeBytes, 0, 4);
                String type = new String(typeBytes);
                //System.out.println(type);

                if (type.equals(target)) {
                    byte[] targetData = new byte[boxSize];
                    System.arraycopy(f.readNBytes(boxSize - 8), 0, targetData, 8, boxSize - 8);
                    //System.out.println(Arrays.toString(targetData));
                    System.arraycopy(sizeBytes, 0, targetData, 0, 4);
                    System.arraycopy(typeBytes, 0, targetData, 4, 4);
                    return targetData;
                }

            }

        }
        catch (Exception ignored){

        }

        return new byte[0];

    }

}
