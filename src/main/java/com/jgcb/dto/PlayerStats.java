package com.jgcb.dto;

// 玩家战绩统计数据, 用于麻将排名榜
public class PlayerStats {
    private Long userId;         // 用户ID
    private String nickname;     // 昵称, 从 sys_user 查得
    private String avatar;       // 头像, 从 sys_user 查得
    private Integer totalScore;  // 累计总分 (正=赢, 负=输)
    private Integer gameCount;   // 参与的已结算局数
    private Integer avgScore;    // 场均分 = totalScore / gameCount

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public Integer getGameCount() { return gameCount; }
    public void setGameCount(Integer gameCount) { this.gameCount = gameCount; }
    public Integer getAvgScore() { return avgScore; }
    public void setAvgScore(Integer avgScore) { this.avgScore = avgScore; }
}
