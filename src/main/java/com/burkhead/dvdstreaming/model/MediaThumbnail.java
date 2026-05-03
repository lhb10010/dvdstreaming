package com.burkhead.dvdstreaming.model;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.exit;

@Entity
public class MediaThumbnail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] imageData;

    //constructor

    public MediaThumbnail() {

    }

    public MediaThumbnail(byte[] imageData) {
        this.imageData = imageData;
    }

    public MediaThumbnail(String path){
        try {
            System.out.println(path);
            this.imageData = Files.readAllBytes(Path.of(path));
        }
        catch(IOException e){
            exit(1);
        }
    }

    //getters

    public byte[] getImageData(){
        return this.imageData;
    }

    public long getId(){
        return this.id;
    }

    //override toString
    @Override
    public String toString() {
        return String.valueOf(this.id);
    }

}
