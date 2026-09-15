package com.jgcb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgcb.common.Result;
import com.jgcb.dto.MemoirCommentVO;
import com.jgcb.dto.MemoirRequest;
import com.jgcb.dto.MemoirVO;
import com.jgcb.service.MemoirCommentService;
import com.jgcb.service.MemoirLikeService;
import com.jgcb.service.UserMemoirService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/memoirs")
public class MemoirController {

    private final UserMemoirService memoirService;
    private final MemoirLikeService memoirLikeService;
    private final MemoirCommentService memoirCommentService;

    public MemoirController(UserMemoirService memoirService,
                            MemoirLikeService memoirLikeService,
                            MemoirCommentService memoirCommentService) {
        this.memoirService = memoirService;
        this.memoirLikeService = memoirLikeService;
        this.memoirCommentService = memoirCommentService;
    }

    @PostMapping
    public Result<Void> create(@RequestBody MemoirRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        memoirService.create(userId, request.getTitle(), request.getContent(), request.getImages());
        return Result.ok();
    }

    @GetMapping
    public Result<Page<MemoirVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        return Result.ok(memoirService.list(page, size));
    }

    @GetMapping("/{id}")
    public Result<MemoirVO> detail(@PathVariable Long id, Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.ok(memoirService.getDetail(id, userId));
    }

    @GetMapping("/user/{userId}")
    public Result<Page<MemoirVO>> listByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(memoirService.listByUserId(userId, page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        memoirService.delete(id, userId);
        return Result.ok();
    }

    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        boolean liked = memoirLikeService.toggleLike(userId, id);
        int count = memoirLikeService.countLikes(id);
        return Result.ok(Map.of("liked", liked, "likeCount", count));
    }

    @GetMapping("/{id}/comments")
    public Result<List<MemoirCommentVO>> getComments(@PathVariable Long id) {
        return Result.ok(memoirCommentService.listByMemoirId(id));
    }

    @PostMapping("/{id}/comments")
    public Result<MemoirCommentVO> addComment(@PathVariable Long id,
                                               @RequestBody Map<String, String> body,
                                               Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        String content = body.get("content");
        memoirCommentService.addComment(userId, id, content);
        return Result.ok(null);
    }

    @DeleteMapping("/{memoirId}/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long memoirId,
                                       @PathVariable Long commentId,
                                       Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        memoirCommentService.deleteComment(commentId, userId);
        return Result.ok();
    }
}
