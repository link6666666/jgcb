package com.jgcb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgcb.dto.MemoirVO;
import com.jgcb.entity.UserMemoir;

import java.util.List;

public interface UserMemoirService extends IService<UserMemoir> {
    void create(Long userId, String title, String content, List<String> images);
    Page<MemoirVO> list(int page, int size);
    Page<MemoirVO> listByUserId(Long userId, int page, int size);
    MemoirVO getDetail(Long id, Long currentUserId);
    void delete(Long memoirId, Long userId);
}
