package com.jgcb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgcb.common.Result;
import com.jgcb.dto.CreateSessionRequest;
import com.jgcb.dto.PlayerStats;
import com.jgcb.dto.SettleRequest;
import com.jgcb.dto.SessionVO;

import java.util.List;
import com.jgcb.service.MahjongSessionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// 麻将模块: 创建/加入/退出/取消/结算牌局, 外加玩家战绩排行
@RestController
@RequestMapping("/api/mahjong")
public class MahjongController {

    private final MahjongSessionService sessionService;

    public MahjongController(MahjongSessionService sessionService) {
        this.sessionService = sessionService;
    }

    // 创建牌局, 发起人自动加入
    @PostMapping("/sessions")
    public Result<Void> create(@RequestBody CreateSessionRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        sessionService.create(userId, request);
        return Result.ok();
    }

    // 分页查询牌局列表, 排除已取消的
    @GetMapping("/sessions")
    public Result<Page<SessionVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(sessionService.list(page, size));
    }

    // 牌局详情, 含参与者信息和结算结果
    @GetMapping("/sessions/{id}")
    public Result<SessionVO> detail(@PathVariable Long id) {
        return Result.ok(sessionService.getDetail(id));
    }

    // 加入牌局, 满员自动标记状态2
    @PostMapping("/sessions/{id}/join")
    public Result<Void> join(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        sessionService.join(id, userId);
        return Result.ok();
    }

    // 退出牌局, 发起人不可退出, 退出后状态重置为1
    @PostMapping("/sessions/{id}/leave")
    public Result<Void> leave(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        sessionService.leave(id, userId);
        return Result.ok();
    }

    // 取消牌局, 仅发起人可操作, 状态置0
    @PostMapping("/sessions/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        sessionService.cancel(id, userId);
        return Result.ok();
    }

    // 结算牌局, 发起人设置每位玩家的输赢分数, 结算后状态=3
    @PostMapping("/sessions/{id}/settle")
    public Result<Void> settle(@PathVariable Long id, @RequestBody SettleRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        sessionService.settle(id, userId, request.getItems());
        return Result.ok();
    }

    // 玩家战绩排行: 汇总所有已结算牌局, 按总分降序
    @GetMapping("/stats")
    public Result<List<PlayerStats>> stats() {
        return Result.ok(sessionService.getStats());
    }
}
