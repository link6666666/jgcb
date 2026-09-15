package com.jgcb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgcb.dto.MemoirCommentVO;
import com.jgcb.entity.MemoirComment;
import com.jgcb.entity.SysUser;
import com.jgcb.mapper.MemoirCommentMapper;
import com.jgcb.service.MemoirCommentService;
import com.jgcb.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemoirCommentServiceImpl extends ServiceImpl<MemoirCommentMapper, MemoirComment> implements MemoirCommentService {

    private final SysUserService sysUserService;

    public MemoirCommentServiceImpl(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    public void addComment(Long userId, Long memoirId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }
        MemoirComment comment = new MemoirComment();
        comment.setUserId(userId);
        comment.setMemoirId(memoirId);
        comment.setContent(content.trim());
        this.save(comment);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        MemoirComment comment = this.getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人的评论");
        }
        this.removeById(commentId);
    }

    @Override
    public List<MemoirCommentVO> listByMemoirId(Long memoirId) {
        List<MemoirComment> comments = this.list(
                new LambdaQueryWrapper<MemoirComment>()
                        .eq(MemoirComment::getMemoirId, memoirId)
                        .orderByAsc(MemoirComment::getCreatedAt)
        );
        return comments.stream().map(c -> {
            MemoirCommentVO vo = new MemoirCommentVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setContent(c.getContent());
            vo.setCreatedAt(c.getCreatedAt());
            SysUser user = sysUserService.getById(c.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                vo.setAvatar(user.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public int countComments(Long memoirId) {
        return (int) this.count(new LambdaQueryWrapper<MemoirComment>()
                .eq(MemoirComment::getMemoirId, memoirId));
    }
}
