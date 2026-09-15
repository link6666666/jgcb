package com.jgcb.dto;

import java.util.List;

public class MemoirRequest {
    private String title;
    private String content;
    private List<String> images;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
