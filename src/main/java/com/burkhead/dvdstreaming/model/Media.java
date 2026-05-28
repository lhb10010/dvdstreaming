package com.burkhead.dvdstreaming.model;


import com.fasterxml.jackson.annotation.JsonGetter;

public interface Media {

    public String getTitle();
    public long getLastTimeWatched();
    public long getId();
    @JsonGetter("progress")
    public long getProgress();

    //@JsonGetter("progressPercent")
    public double getProgressPercent();

    @JsonGetter("type")
    public String getType();


}
