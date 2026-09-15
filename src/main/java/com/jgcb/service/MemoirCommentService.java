package com.jgcb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgcb.dto.MemoirCommentVO;
import com.jgcb.entity.MemoirComment;

import java.util.List;

public interface MemoirCommentService extends IService<MemoirComment> {
    void addComment(Long userId, Long memoirId, String content);
    void deleteComment(Long commentId, Long userId);
    List<MemoirCommentVO> listByMemoirId(Long memoirId);
    int countComments(Long memoirId);
}
