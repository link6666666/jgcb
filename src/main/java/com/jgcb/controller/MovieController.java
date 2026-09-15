package com.jgcb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgcb.common.Result;
import com.jgcb.dto.MovieVO;
import com.jgcb.dto.CreateMovieRequest;
import com.jgcb.service.MovieSessionService;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movie")
public class MovieController {

    private final MovieSessionService movieService;

    public MovieController(MovieSessionService movieService) {
        this.movieService = movieService;
    }

    @PostMapping("/sessions")
    public Result<Void> create(@RequestBody CreateMovieRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        movieService.create(userId, request);
        return Result.ok();
    }

    @GetMapping("/sessions")
    public Result<Page<MovieVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(movieService.list(page, size));
    }

    @GetMapping("/sessions/{id}")
    public Result<MovieVO> detail(@PathVariable Long id) {
        return Result.ok(movieService.getDetail(id));
    }

    @PostMapping("/sessions/{id}/join")
    public Result<Void> join(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        movieService.join(id, userId);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/leave")
    public Result<Void> leave(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        movieService.leave(id, userId);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        movieService.cancel(id, userId);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/images")
    public Result<Void> uploadImages(@PathVariable Long id, @RequestBody List<String> images, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        movieService.uploadImages(id, userId, images);
        return Result.ok();
    }

    @PostMapping("/sessions/{id}/finish")
    public Result<Void> finish(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        movieService.finish(id, userId);
        return Result.ok();
    }
}
