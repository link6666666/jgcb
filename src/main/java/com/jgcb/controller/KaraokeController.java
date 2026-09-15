package com.jgcb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgcb.common.Result;
import com.jgcb.dto.KaraokeVO;
import com.jgcb.dto.CreateKaraokeRequest;
import com.jgcb.dto.SettleRequest;
import com.jgcb.service.KaraokeSessionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/karaoke")
public class KaraokeController {

    private final KaraokeSessionService karaokeService;

    public KaraokeController(KaraokeSessionService karaokeService) {
        this.karaokeService = karaokeService;
    }

    @PostMapping("/sessions")
    public Result<Void> create(@RequestBody CreateKaraokeRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        karaokeService.create(userId, request);
        return Result.ok();
    }

    @GetMapping("/sessions")
    public Result<Page<KaraokeVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(karaokeService.list(page, size));
    }

    @GetMapping("/sessions/{id}")
    public Result<KaraokeVO> detail(@PathVariable Long id) {
        return Result.ok(karaokeService.getDetail(id));
    }

    @PostMapping("/sessions/{id}/join")
    public Result<Void> join(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        karaokeService.join(id, userId);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/leave")
    public Result<Void> leave(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        karaokeService.leave(id, userId);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        karaokeService.cancel(id, userId);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/settle")
    public Result<Void> settle(@PathVariable Long id, @RequestBody SettleRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        karaokeService.settle(id, userId, request.getItems());
        return Result.ok();
    }
}
