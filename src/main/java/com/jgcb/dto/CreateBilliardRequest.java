package com.jgcb.dto;

import java.time.LocalDateTime;

public class CreateBilliardRequest {
    private String title;
    private String location;
    private LocalDateTime sessionTime;
    private Integer maxPlayers;
    private String remark;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getSessionTime() { return sessionTime; }
    public void setSessionTime(LocalDateTime sessionTime) { this.sessionTime = sessionTime; }
    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
