package com.jgcb.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PlayerStateRequest {
    @NotNull(message = "横坐标不能为空")
    @Min(value = 0, message = "横坐标超出房间")
    @Max(value = 1200, message = "横坐标超出房间")
    private Integer x;

    @NotNull(message = "纵坐标不能为空")
    @Min(value = 0, message = "纵坐标超出房间")
    @Max(value = 800, message = "纵坐标超出房间")
    private Integer y;

    @Size(max = 20, message = "方向参数过长")
    private String direction;

    @Size(max = 40, message = "动作参数过长")
    private String action;

    public Integer getX() { return x; }
    public void setX(Integer x) { this.x = x; }
    public Integer getY() { return y; }
    public void setY(Integer y) { this.y = y; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}