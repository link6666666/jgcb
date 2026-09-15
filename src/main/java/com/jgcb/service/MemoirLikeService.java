package com.jgcb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgcb.entity.MemoirLike;

public interface MemoirLikeService extends IService<MemoirLike> {
    boolean toggleLike(Long userId, Long memoirId);
    int countLikes(Long memoirId);
    boolean isLiked(Long userId, Long memoirId);
}
