package com.jgcb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgcb.common.Result;
import com.jgcb.dto.BilliardVO;
import com.jgcb.dto.CreateBilliardRequest;
import com.jgcb.dto.SettleRequest;
import com.jgcb.service.BilliardSessionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billiard")
public class BilliardController {

    private final BilliardSessionService billiardService;

    public BilliardController(BilliardSessionService billiardService) {
        this.billiardService = billiardService;
    }

    @PostMapping("/sessions")
    public Result<Void> create(@RequestBody CreateBilliardRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        billiardService.create(userId, request);
        return Result.ok();
    }

    @GetMapping("/sessions")
    public Result<Page<BilliardVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(billiardService.list(page, size));
    }

    @GetMapping("/sessions/{id}")
    public Result<BilliardVO> detail(@PathVariable Long id) {
        return Result.ok(billiardService.getDetail(id));
    }

    @PostMapping("/sessions/{id}/join")
    public Result<Void> join(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        billiardService.join(id, userId);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/leave")
    public Result<Void> leave(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        billiardService.leave(id, userId);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        billiardService.cancel(id, userId);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/settle")
    public Result<Void> settle(@PathVariable Long id, @RequestBody SettleRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        billiardService.settle(id, userId, request.getItems());
        return Result.ok();
    }
}
