package com.jgcb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgcb.dto.CreateSessionRequest;
import com.jgcb.dto.PlayerInfo;
import com.jgcb.dto.PlayerStats;
import com.jgcb.dto.SessionVO;
import com.jgcb.dto.SettleItem;
import com.jgcb.entity.MahjongSession;
import com.jgcb.entity.SysUser;
import com.jgcb.mapper.MahjongSessionMapper;
import com.jgcb.service.MahjongSessionService;
import com.jgcb.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MahjongSessionServiceImpl extends ServiceImpl<MahjongSessionMapper, MahjongSession> implements MahjongSessionService {

    private final SysUserService sysUserService;
    private final ObjectMapper objectMapper;

    public MahjongSessionServiceImpl(SysUserService sysUserService, ObjectMapper objectMapper) {
        this.sysUserService = sysUserService;
        this.objectMapper = objectMapper;
    }

    // 创建牌局: 发起人自动加入, 状态=1(进行中), 人数上限默认4人
    @Override
    public void create(Long userId, CreateSessionRequest request) {
        MahjongSession session = new MahjongSession();
        session.setCreatorId(userId);
        session.setTitle(request.getTitle());
        session.setLocation(request.getLocation());
        session.setSessionTime(request.getSessionTime());
        session.setMaxPlayers(request.getMaxPlayers() != null ? request.getMaxPlayers() : 4);
        session.setRemark(request.getRemark());
        session.setStatus(1);
        try {
            // 发起人自动加入, playerIds 序列化为 JSON 数组
            session.setPlayerIds(objectMapper.writeValueAsString(Arrays.asList(userId)));
        } catch (JsonProcessingException e) {
            session.setPlayerIds("[]");
        }
        this.save(session);
    }

    // 加入牌局: 仅状态=1时可加入, 满员自动切状态=2
    @Override
    public void join(Long sessionId, Long userId) {
        MahjongSession session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("麻将局不存在");
        }
        // 仅进行中状态可加入, 已满员/已结算/已取消均不可加入
        if (session.getStatus() != 1) {
            throw new RuntimeException("该局已结束或已取消");
        }
        List<Long> playerIds = parsePlayerIds(session.getPlayerIds());
        if (playerIds.contains(userId)) {
            throw new RuntimeException("你已经在牌局中了");
        }
        if (playerIds.size() >= session.getMaxPlayers()) {
            throw new RuntimeException("人数已满");
        }
        playerIds.add(userId);
        try {
            session.setPlayerIds(objectMapper.writeValueAsString(playerIds));
        } catch (JsonProcessingException e) { /* ignore */ }
        // 满员时自动标记为状态2, 不再接受新玩家
        if (playerIds.size() >= session.getMaxPlayers()) {
            session.setStatus(2);
        }
        this.updateById(session);
    }

    // 退出牌局: 发起人不可退出(应取消), 退出后状态重置为1
    @Override
    public void leave(Long sessionId, Long userId) {
        MahjongSession session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("麻将局不存在");
        }
        // 发起人不能退出, 只能取消整个牌局
        if (session.getCreatorId().equals(userId)) {
            throw new RuntimeException("发起人不能退出，请取消牌局");
        }
        List<Long> playerIds = parsePlayerIds(session.getPlayerIds());
        playerIds.remove(userId);
        try {
            session.setPlayerIds(objectMapper.writeValueAsString(playerIds));
        } catch (JsonProcessingException e) { /* ignore */ }
        // 有人退出后重置为进行中, 允许新玩家加入
        session.setStatus(1);
        this.updateById(session);
    }

    // 取消牌局: 仅发起人可操作, 状态置为0
    @Override
    public void cancel(Long sessionId, Long userId) {
        MahjongSession session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("麻将局不存在");
        }
        if (!session.getCreatorId().equals(userId)) {
            throw new RuntimeException("只有发起人可以取消");
        }
        session.setStatus(0);
        this.updateById(session);
    }

    // 结算: 仅发起人可操作, 状态1或2时均可结算, 结算后状态=3
    // items 为 [{userId, score}] 输赢分数, 序列化存到 result 字段
    @Override
    public void settle(Long sessionId, Long userId, List<SettleItem> items) {
        MahjongSession session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("麻将局不存在");
        }
        if (!session.getCreatorId().equals(userId)) {
            throw new RuntimeException("只有发起人可以结算");
        }
        // 已取消(0)和已结算(3)的不能再结算
        if (session.getStatus() != 1 && session.getStatus() != 2) {
            throw new RuntimeException("当前状态不可结算");
        }
        try {
            session.setResult(objectMapper.writeValueAsString(items));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("结算数据格式错误");
        }
        session.setStatus(3);
        this.updateById(session);
    }

    // 牌局列表: 排除已取消的, 按时间倒序, 分页返回
    @Override
    public Page<SessionVO> list(int page, int size) {
        Page<MahjongSession> sessionPage = this.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<MahjongSession>()
                        .in(MahjongSession::getStatus, 1, 2, 3) // 排除已取消的
                        .orderByDesc(MahjongSession::getSessionTime)
        );
        return toSessionVOPage(sessionPage);
    }

    @Override
    public SessionVO getDetail(Long sessionId) {
        MahjongSession session = this.getById(sessionId);
        if (session == null) return null;
        return toSessionVO(session);
    }

    // 玩家统计: 遍历所有已结算牌局, 累加每个玩家总分/局数/均分, 按总分降序
    @Override
    public List<PlayerStats> getStats() {
        // 只统计已结算(status=3)的牌局
        List<MahjongSession> settled = this.list(
                new LambdaQueryWrapper<MahjongSession>().eq(MahjongSession::getStatus, 3)
        );
        // map: userId -> [累计总分, 参与局数]
        Map<Long, int[]> scoreMap = new HashMap<>();
        for (MahjongSession s : settled) {
            List<SettleItem> items = parseResult(s.getResult());
            for (SettleItem item : items) {
                int[] arr = scoreMap.computeIfAbsent(item.getUserId(), k -> new int[2]);
                arr[0] += item.getScore() != null ? item.getScore() : 0;
                arr[1] += 1;
            }
        }
        List<PlayerStats> stats = new ArrayList<>();
        for (Map.Entry<Long, int[]> e : scoreMap.entrySet()) {
            Long uid = e.getKey();
            int[] arr = e.getValue();
            PlayerStats ps = new PlayerStats();
            ps.setUserId(uid);
            ps.setTotalScore(arr[0]);
            ps.setGameCount(arr[1]);
            // 均分 = 总分 / 局数 (整数除法)
            ps.setAvgScore(arr[1] > 0 ? arr[0] / arr[1] : 0);
            // 查用户昵称/头像, 用于前端排名展示
            SysUser u = sysUserService.getById(uid);
            if (u != null) {
                ps.setNickname(u.getNickname() != null ? u.getNickname() : u.getUsername());
                ps.setAvatar(u.getAvatar());
            }
            stats.add(ps);
        }
        // 按总分降序排列
        stats.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));
        return stats;
    }

    // 分页实体转分页VO, 逐条调用 toSessionVO 填充用户信息
    private Page<SessionVO> toSessionVOPage(IPage<MahjongSession> page) {
        Page<SessionVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toSessionVO).collect(Collectors.toList()));
        return voPage;
    }

    // 实体转VO: 通过 sysUserService 查询发起人和参与者昵称/头像
    private SessionVO toSessionVO(MahjongSession session) {
        SessionVO vo = new SessionVO();
        vo.setId(session.getId());
        vo.setCreatorId(session.getCreatorId());
        vo.setTitle(session.getTitle());
        vo.setLocation(session.getLocation());
        vo.setSessionTime(session.getSessionTime());
        vo.setMaxPlayers(session.getMaxPlayers());
        vo.setRemark(session.getRemark());
        vo.setStatus(session.getStatus());
        vo.setCreatedAt(session.getCreatedAt());

        // 查询发起人昵称和头像
        SysUser creator = sysUserService.getById(session.getCreatorId());
        if (creator != null) {
            vo.setCreatorName(creator.getNickname() != null ? creator.getNickname() : creator.getUsername());
            vo.setCreatorAvatar(creator.getAvatar());
        }

        // 解析 playerIds JSON, 逐个查询用户信息
        List<Long> playerIds = parsePlayerIds(session.getPlayerIds());
        List<PlayerInfo> players = new ArrayList<>();
        for (Long pid : playerIds) {
            SysUser u = sysUserService.getById(pid);
            if (u != null) {
                players.add(new PlayerInfo(pid,
                        u.getNickname() != null ? u.getNickname() : u.getUsername(),
                        u.getAvatar()));
            }
        }
        vo.setPlayers(players);

        // 如果有结算结果, 将分数合并到玩家列表中
        if (session.getResult() != null) {
            List<SettleItem> settleItems = parseResult(session.getResult());
            Map<Long, Integer> scoreMap = new HashMap<>();
            for (SettleItem item : settleItems) {
                scoreMap.put(item.getUserId(), item.getScore());
            }
            List<PlayerInfo> resultList = new ArrayList<>();
            for (PlayerInfo p : players) {
                PlayerInfo r = new PlayerInfo(p.getUserId(), p.getNickname(), p.getAvatar());
                r.setScore(scoreMap.get(p.getUserId())); // 未出现在结算中的玩家分数为null
                resultList.add(r);
            }
            vo.setResult(resultList);
        }

        return vo;
    }

    // 从 JSON 字符串解析玩家ID列表, 解析失败返回空列表
    private List<Long> parsePlayerIds(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // 从 JSON 字符串解析结算结果列表
    private List<SettleItem> parseResult(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<SettleItem>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
