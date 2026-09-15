package com.jgcb.dto;

public class PetDecorationVO {
    private String code;
    private String slot;
    private String emoji;
    private String name;
    private Integer unlockLevel;

    public PetDecorationVO() {}

    public PetDecorationVO(String code, String slot, String emoji, String name, Integer unlockLevel) {
        this.code = code;
        this.slot = slot;
        this.emoji = emoji;
        this.name = name;
        this.unlockLevel = unlockLevel;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getUnlockLevel() { return unlockLevel; }
    public void setUnlockLevel(Integer unlockLevel) { this.unlockLevel = unlockLevel; }
}
