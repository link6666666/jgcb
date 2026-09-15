package com.jgcb.dto;

public class RoomPlayerVO {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer x;
    private Integer y;
    private String direction;
    private String action;
    private Boolean online;
    private String head;
    private String neck;
    private String body;
    private String accessory;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getX() { return x; }
    public void setX(Integer x) { this.x = x; }
    public Integer getY() { return y; }
    public void setY(Integer y) { this.y = y; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Boolean getOnline() { return online; }
    public void setOnline(Boolean online) { this.online = online; }
    public String getHead() { return head; }
    public void setHead(String head) { this.head = head; }
    public String getNeck() { return neck; }
    public void setNeck(String neck) { this.neck = neck; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getAccessory() { return accessory; }
    public void setAccessory(String accessory) { this.accessory = accessory; }
}