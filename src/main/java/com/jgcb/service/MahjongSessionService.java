package com.jgcb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgcb.dto.CreateSessionRequest;
import com.jgcb.dto.PlayerStats;
import com.jgcb.dto.SettleItem;
import com.jgcb.dto.SessionVO;
import com.jgcb.entity.MahjongSession;

import java.util.List;

public interface MahjongSessionService extends IService<MahjongSession> {
    void create(Long userId, CreateSessionRequest request);          // 创建牌局, 发起人自动加入
    void join(Long sessionId, Long userId);                          // 加入, 满员自动标记状态2
    void leave(Long sessionId, Long userId);                         // 退出, 发起人不可退出
    void cancel(Long sessionId, Long userId);                        // 取消, 仅发起人
    void settle(Long sessionId, Long userId, List<SettleItem> items);// 结算, 发起人设定每人分数
    Page<SessionVO> list(int page, int size);                        // 分页列表, 排除已取消
    SessionVO getDetail(Long sessionId);                             // 详情, 含玩家和结算结果
    List<PlayerStats> getStats();                                    // 全局玩家战绩排行
}
