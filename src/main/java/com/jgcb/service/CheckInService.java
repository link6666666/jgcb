package com.jgcb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgcb.entity.CheckIn;

import java.time.LocalDate;
import java.util.List;

public interface CheckInService extends IService<CheckIn> {

    boolean checkIn(Long userId);

    boolean isCheckedInToday(Long userId);

    int getStreak(Long userId);

    List<LocalDate> getMonthlyCheckIns(Long userId, int year, int month);
}
