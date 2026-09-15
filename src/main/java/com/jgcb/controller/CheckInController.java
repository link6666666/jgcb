package com.jgcb.controller;

import com.jgcb.common.Result;
import com.jgcb.dto.CheckInStatusVO;
import com.jgcb.service.CheckInService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping
    public Result<String> checkIn(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        try {
            boolean success = checkInService.checkIn(userId);
            if (!success) {
                return Result.fail(400, "今天已经签到过了");
            }
            return Result.ok("签到成功");
        } catch (DuplicateKeyException e) {
            return Result.fail(400, "今天已经签到过了");
        }
    }

    @GetMapping("/status")
    public Result<CheckInStatusVO> status(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        CheckInStatusVO vo = new CheckInStatusVO();
        vo.setCheckedInToday(checkInService.isCheckedInToday(userId));
        vo.setStreak(checkInService.getStreak(userId));
        return Result.ok(vo);
    }

    @GetMapping("/calendar")
    public Result<List<LocalDate>> calendar(
            @RequestParam int year,
            @RequestParam int month,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(checkInService.getMonthlyCheckIns(userId, year, month));
    }
}
