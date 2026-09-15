package com.jgcb.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CreateMovieRequest {
    private String title;
    private String movieName;
    private String location;
    private LocalDateTime sessionTime;
    private Integer maxPlayers;
    private String remark;
    private List<String> images;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMovieName() { return movieName; }
    public void setMovieName(String movieName) { this.movieName = movieName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getSessionTime() { return sessionTime; }
    public void setSessionTime(LocalDateTime sessionTime) { this.sessionTime = sessionTime; }
    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
