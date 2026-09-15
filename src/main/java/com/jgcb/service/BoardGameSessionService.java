package com.jgcb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgcb.dto.BoardGameVO;
import com.jgcb.dto.CreateBoardGameRequest;
import com.jgcb.dto.SettleItem;
import com.jgcb.entity.BoardGameSession;

import java.util.List;

public interface BoardGameSessionService extends IService<BoardGameSession> {
    void create(Long userId, CreateBoardGameRequest request);
    void join(Long sessionId, Long userId);
    void leave(Long sessionId, Long userId);
    void cancel(Long sessionId, Long userId);
    void settle(Long sessionId, Long userId, List<SettleItem> items);
    Page<BoardGameVO> list(int page, int size);
    BoardGameVO getDetail(Long sessionId);
}
