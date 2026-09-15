package com.jgcb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("pet_outfit")
public class PetOutfit {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String head;
    private String face;
    private String neck;
    private String body;
    private String accessory;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getHead() { return head; }
    public void setHead(String head) { this.head = head; }
    public String getFace() { return face; }
    public void setFace(String face) { this.face = face; }
    public String getNeck() { return neck; }
    public void setNeck(String neck) { this.neck = neck; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getAccessory() { return accessory; }
    public void setAccessory(String accessory) { this.accessory = accessory; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
