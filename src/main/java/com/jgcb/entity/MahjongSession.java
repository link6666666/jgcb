package com.jgcb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

// 状态约定: 0=已取消, 1=进行中(可加入), 2=已满员, 3=已结算
// playerIds 和 result 以 JSON 字符串存储 (如 "[1,2,3]"), 非标准外键表
@TableName("mahjong_session")
public class MahjongSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long creatorId;           // 发起人用户ID
    private String title;             // 牌局标题
    private String location;          // 地点; 前端用 <select> 预设选项
    private LocalDateTime sessionTime; // 牌局时间
    private Integer maxPlayers;       // 人数上限, 默认4
    private String playerIds;         // JSON数组, 参与者用户ID列表
    private String remark;            // 备注
    private String result;            // JSON数组, 结算后存储 [{userId, score}]
    private Integer status;           // 0=取消 1=进行中 2=满员 3=已结算
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getSessionTime() { return sessionTime; }
    public void setSessionTime(LocalDateTime sessionTime) { this.sessionTime = sessionTime; }
    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }
    public String getPlayerIds() { return playerIds; }
    public void setPlayerIds(String playerIds) { this.playerIds = playerIds; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
