package com.jgcb.dto;

import java.time.LocalDate;
import java.util.List;

public class TravelRequest {
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String plan;
    private String process;
    private Integer participantCount;
    private List<String> images;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public String getProcess() { return process; }
    public void setProcess(String process) { this.process = process; }
    public Integer getParticipantCount() { return participantCount; }
    public void setParticipantCount(Integer participantCount) { this.participantCount = participantCount; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
