package com.jgcb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgcb.common.Result;
import com.jgcb.dto.BoardGameVO;
import com.jgcb.dto.CreateBoardGameRequest;
import com.jgcb.dto.SettleRequest;
import com.jgcb.service.BoardGameSessionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boardgame")
public class BoardGameController {

    private final BoardGameSessionService boardGameService;

    public BoardGameController(BoardGameSessionService boardGameService) {
        this.boardGameService = boardGameService;
    }

    @PostMapping("/sessions")
    public Result<Void> create(@RequestBody CreateBoardGameRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        boardGameService.create(userId, request);
        return Result.ok();
    }

    @GetMapping("/sessions")
    public Result<Page<BoardGameVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(boardGameService.list(page, size));
    }

    @GetMapping("/sessions/{id}")
    public Result<BoardGameVO> detail(@PathVariable Long id) {
        return Result.ok(boardGameService.getDetail(id));
    }

    @PostMapping("/sessions/{id}/join")
    public Result<Void> join(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        boardGameService.join(id, userId);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/leave")
    public Result<Void> leave(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        boardGameService.leave(id, userId);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        boardGameService.cancel(id, userId);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/settle")
    public Result<Void> settle(@PathVariable Long id, @RequestBody SettleRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        boardGameService.settle(id, userId, request.getItems());
        return Result.ok();
    }
}
