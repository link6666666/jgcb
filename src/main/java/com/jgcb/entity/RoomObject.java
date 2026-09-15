package com.jgcb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("room_object")
public class RoomObject {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private String type;
    private Integer x;
    private Integer y;
    private String state;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getX() { return x; }
    public void setX(Integer x) { this.x = x; }
    public Integer getY() { return y; }
    public void setY(Integer y) { this.y = y; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}