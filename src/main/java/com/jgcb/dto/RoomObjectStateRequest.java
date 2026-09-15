package com.jgcb.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RoomObjectStateRequest {
    @NotNull(message = "物品状态不能为空")
    @Size(max = 2000, message = "物品状态过长")
    private String state;

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}