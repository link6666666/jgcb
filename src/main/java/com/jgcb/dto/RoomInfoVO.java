package com.jgcb.dto;

import com.jgcb.entity.RoomObject;
import java.util.List;

public class RoomInfoVO {
    private Long id;
    private String name;
    private String mapImage;
    private Integer mapWidth;
    private Integer mapHeight;
    private List<RoomObject> objects;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMapImage() { return mapImage; }
    public void setMapImage(String mapImage) { this.mapImage = mapImage; }
    public Integer getMapWidth() { return mapWidth; }
    public void setMapWidth(Integer mapWidth) { this.mapWidth = mapWidth; }
    public Integer getMapHeight() { return mapHeight; }
    public void setMapHeight(Integer mapHeight) { this.mapHeight = mapHeight; }
    public List<RoomObject> getObjects() { return objects; }
    public void setObjects(List<RoomObject> objects) { this.objects = objects; }
}