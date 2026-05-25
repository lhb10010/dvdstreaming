package com.burkhead.dvdstreaming.model;


import com.fasterxml.jackson.annotation.JsonGetter;

public interface Media {

    public String getTitle();
    public long getLastTimeWatched();

    @JsonGetter("type")
    public String getType();
    //public byte[] getThumbnail();
    //public String getThumbnailB64();

}
