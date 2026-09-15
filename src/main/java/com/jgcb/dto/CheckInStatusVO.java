package com.jgcb.dto;

public class CheckInStatusVO {

    private boolean checkedInToday;
    private int streak;

    public boolean isCheckedInToday() { return checkedInToday; }
    public void setCheckedInToday(boolean checkedInToday) { this.checkedInToday = checkedInToday; }
    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }
}
