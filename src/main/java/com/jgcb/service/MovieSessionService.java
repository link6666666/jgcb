package com.jgcb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgcb.dto.MovieVO;
import com.jgcb.dto.CreateMovieRequest;
import com.jgcb.entity.MovieSession;

import java.util.List;

public interface MovieSessionService extends IService<MovieSession> {
    void create(Long userId, CreateMovieRequest request);
    void join(Long sessionId, Long userId);
    void leave(Long sessionId, Long userId);
    void cancel(Long sessionId, Long userId);
    void uploadImages(Long sessionId, Long userId, List<String> images);
    void finish(Long sessionId, Long userId);
    Page<MovieVO> list(int page, int size);
    MovieVO getDetail(Long sessionId);
}
