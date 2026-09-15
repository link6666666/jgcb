package com.jgcb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgcb.entity.CheckIn;
import com.jgcb.entity.SysUser;
import com.jgcb.mapper.CheckInMapper;
import com.jgcb.mapper.SysUserMapper;
import com.jgcb.service.CheckInService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckInService {

    private final SysUserMapper sysUserMapper;

    public CheckInServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public boolean checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        long count = this.count(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId)
                .eq(CheckIn::getCheckDate, today));
        if (count > 0) {
            return false;
        }
        CheckIn record = new CheckIn();
        record.setUserId(userId);
        record.setCheckDate(today);
        try {
            this.save(record);
        } catch (DuplicateKeyException e) {
            return false;
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            int newExp = (user.getExp() == null ? 0 : user.getExp()) + 10;
            sysUserMapper.update(null,
                    new LambdaUpdateWrapper<SysUser>()
                            .eq(SysUser::getId, userId)
                            .set(SysUser::getExp, newExp));
        }
        return true;
    }

    @Override
    public boolean isCheckedInToday(Long userId) {
        LocalDate today = LocalDate.now();
        return this.count(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId)
                .eq(CheckIn::getCheckDate, today)) > 0;
    }

    @Override
    public int getStreak(Long userId) {
        List<LocalDate> dates = this.list(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId)
                .orderByDesc(CheckIn::getCheckDate))
                .stream()
                .map(CheckIn::getCheckDate)
                .collect(Collectors.toList());

        if (dates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate first = dates.get(0);
        if (!first.equals(today) && !first.equals(today.minusDays(1))) {
            return 0;
        }

        int streak = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i - 1).minusDays(1).equals(dates.get(i))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    @Override
    public List<LocalDate> getMonthlyCheckIns(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);
        return this.list(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId)
                .ge(CheckIn::getCheckDate, start)
                .lt(CheckIn::getCheckDate, end))
                .stream()
                .map(CheckIn::getCheckDate)
                .collect(Collectors.toList());
    }
}
