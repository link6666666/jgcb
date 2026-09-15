package com.jgcb.dto;

import java.util.List;

public class PetStatusVO {
    private Integer level;
    private Integer exp;
    private String head;
    private String face;
    private String neck;
    private String body;
    private String accessory;
    private List<PetDecorationVO> decorations;

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getExp() { return exp; }
    public void setExp(Integer exp) { this.exp = exp; }
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
    public List<PetDecorationVO> getDecorations() { return decorations; }
    public void setDecorations(List<PetDecorationVO> decorations) { this.decorations = decorations; }
}
