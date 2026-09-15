package com.jgcb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgcb.entity.MemoirLike;
import com.jgcb.mapper.MemoirLikeMapper;
import com.jgcb.service.MemoirLikeService;
import org.springframework.stereotype.Service;

@Service
public class MemoirLikeServiceImpl extends ServiceImpl<MemoirLikeMapper, MemoirLike> implements MemoirLikeService {

    @Override
    public boolean toggleLike(Long userId, Long memoirId) {
        LambdaQueryWrapper<MemoirLike> wrapper = new LambdaQueryWrapper<MemoirLike>()
                .eq(MemoirLike::getUserId, userId)
                .eq(MemoirLike::getMemoirId, memoirId);
        MemoirLike existing = this.getOne(wrapper);
        if (existing != null) {
            this.removeById(existing.getId());
            return false; // unliked
        }
        MemoirLike like = new MemoirLike();
        like.setUserId(userId);
        like.setMemoirId(memoirId);
        this.save(like);
        return true; // liked
    }

    @Override
    public int countLikes(Long memoirId) {
        return (int) this.count(new LambdaQueryWrapper<MemoirLike>()
                .eq(MemoirLike::getMemoirId, memoirId));
    }

    @Override
    public boolean isLiked(Long userId, Long memoirId) {
        if (userId == null) return false;
        return this.count(new LambdaQueryWrapper<MemoirLike>()
                .eq(MemoirLike::getUserId, userId)
                .eq(MemoirLike::getMemoirId, memoirId)) > 0;
    }
}
