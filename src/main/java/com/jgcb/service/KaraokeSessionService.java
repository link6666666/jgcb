package com.jgcb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgcb.dto.KaraokeVO;
import com.jgcb.dto.CreateKaraokeRequest;
import com.jgcb.dto.SettleItem;
import com.jgcb.entity.KaraokeSession;

import java.util.List;

public interface KaraokeSessionService extends IService<KaraokeSession> {
    void create(Long userId, CreateKaraokeRequest request);
    void join(Long sessionId, Long userId);
    void leave(Long sessionId, Long userId);
    void cancel(Long sessionId, Long userId);
    void settle(Long sessionId, Long userId, List<SettleItem> items);
    Page<KaraokeVO> list(int page, int size);
    KaraokeVO getDetail(Long sessionId);
}
