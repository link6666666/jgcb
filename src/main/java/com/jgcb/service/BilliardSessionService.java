package com.jgcb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgcb.dto.BilliardVO;
import com.jgcb.dto.CreateBilliardRequest;
import com.jgcb.dto.SettleItem;
import com.jgcb.entity.BilliardSession;

import java.util.List;

public interface BilliardSessionService extends IService<BilliardSession> {
    void create(Long userId, CreateBilliardRequest request);
    void join(Long sessionId, Long userId);
    void leave(Long sessionId, Long userId);
    void cancel(Long sessionId, Long userId);
    void settle(Long sessionId, Long userId, List<SettleItem> items);
    Page<BilliardVO> list(int page, int size);
    BilliardVO getDetail(Long sessionId);
}
