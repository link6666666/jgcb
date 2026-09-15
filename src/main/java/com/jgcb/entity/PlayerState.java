package com.jgcb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("player_state")
public class PlayerState {
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    private Long roomId;
    private Integer x;
    private Integer y;
    private String action;
    private Integer online;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public Integer getX() { return x; }
    public void setX(Integer x) { this.x = x; }
    public Integer getY() { return y; }
    public void setY(Integer y) { this.y = y; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Integer getOnline() { return online; }
    public void setOnline(Integer online) { this.online = online; }
}