package com.jgcb.dto;

public class PlayerInfo {
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer score;

    public PlayerInfo() {}
    public PlayerInfo(Long userId, String nickname, String avatar) {
        this.userId = userId;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}
