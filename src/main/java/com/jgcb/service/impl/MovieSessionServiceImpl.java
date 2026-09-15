package com.jgcb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgcb.dto.MovieVO;
import com.jgcb.dto.CreateMovieRequest;
import com.jgcb.dto.PlayerInfo;
import com.jgcb.dto.SettleItem;
import com.jgcb.entity.MovieSession;
import com.jgcb.entity.SysUser;
import com.jgcb.mapper.MovieSessionMapper;
import com.jgcb.service.MovieSessionService;
import com.jgcb.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieSessionServiceImpl extends ServiceImpl<MovieSessionMapper, MovieSession> implements MovieSessionService {

    private final SysUserService sysUserService;
    private final ObjectMapper objectMapper;

    public MovieSessionServiceImpl(SysUserService sysUserService, ObjectMapper objectMapper) {
        this.sysUserService = sysUserService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void create(Long userId, CreateMovieRequest request) {
        MovieSession session = new MovieSession();
        session.setCreatorId(userId);
        session.setTitle(request.getTitle());
        session.setMovieName(request.getMovieName());
        session.setLocation(request.getLocation());
        session.setSessionTime(request.getSessionTime());
        session.setMaxPlayers(request.getMaxPlayers() != null ? request.getMaxPlayers() : 4);
        session.setRemark(request.getRemark());
        session.setStatus(1);
        try {
            session.setPlayerIds(objectMapper.writeValueAsString(Arrays.asList(userId)));
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                session.setImages(objectMapper.writeValueAsString(request.getImages()));
            }
        } catch (JsonProcessingException e) {
            session.setPlayerIds("[]");
        }
        this.save(session);
    }

    @Override
    public void join(Long sessionId, Long userId) {
        MovieSession session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("电影局不存在");
        }
        if (session.getStatus() != 1) {
            throw new RuntimeException("该局已结束或已取消");
        }
        List<Long> playerIds = parsePlayerIds(session.getPlayerIds());
        if (playerIds.contains(userId)) {
            throw new RuntimeException("你已经在电影局中了");
        }
        if (playerIds.size() >= session.getMaxPlayers()) {
            throw new RuntimeException("人数已满");
        }
        playerIds.add(userId);
        try {
            session.setPlayerIds(objectMapper.writeValueAsString(playerIds));
        } catch (JsonProcessingException e) { /* ignore */ }
        if (playerIds.size() >= session.getMaxPlayers()) {
            session.setStatus(2);
        }
        this.updateById(session);
    }

    @Override
    public void leave(Long sessionId, Long userId) {
        MovieSession session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("电影局不存在");
        }
        if (session.getCreatorId().equals(userId)) {
            throw new RuntimeException("发起人不能退出，请取消电影局");
        }
        List<Long> playerIds = parsePlayerIds(session.getPlayerIds());
        playerIds.remove(userId);
        try {
            session.setPlayerIds(objectMapper.writeValueAsString(playerIds));
        } catch (JsonProcessingException e) { /* ignore */ }
        session.setStatus(1);
        this.updateById(session);
    }

    @Override
    public void cancel(Long sessionId, Long userId) {
        MovieSession session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("电影局不存在");
        }
        if (!session.getCreatorId().equals(userId)) {
            throw new RuntimeException("只有发起人可以取消");
        }
        session.setStatus(0);
        this.updateById(session);
    }

    @Override
    public void uploadImages(Long sessionId, Long userId, List<String> images) {
        MovieSession session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("电影局不存在");
        }
        List<Long> playerIds = parsePlayerIds(session.getPlayerIds());
        if (!playerIds.contains(userId)) {
            throw new RuntimeException("只有参与者可以上传照片");
        }
        List<String> existing = parseStringList(session.getImages());
        existing.addAll(images);
        try {
            session.setImages(objectMapper.writeValueAsString(existing));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("图片数据格式错误");
        }
        this.updateById(session);
    }

    @Override
    public void finish(Long sessionId, Long userId) {
        MovieSession session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("电影局不存在");
        }
        if (!session.getCreatorId().equals(userId)) {
            throw new RuntimeException("只有发起人可以完结");
        }
        if (session.getStatus() != 1 && session.getStatus() != 2) {
            throw new RuntimeException("当前状态不可完结");
        }
        session.setStatus(3);
        this.updateById(session);
    }

    @Override
    public Page<MovieVO> list(int page, int size) {
        Page<MovieSession> sessionPage = this.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<MovieSession>()
                        .in(MovieSession::getStatus, 1, 2, 3)
                        .orderByDesc(MovieSession::getSessionTime)
        );
        return toVOPage(sessionPage);
    }

    @Override
    public MovieVO getDetail(Long sessionId) {
        MovieSession session = this.getById(sessionId);
        if (session == null) return null;
        return toVO(session);
    }

    private Page<MovieVO> toVOPage(IPage<MovieSession> page) {
        Page<MovieVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    private MovieVO toVO(MovieSession session) {
        MovieVO vo = new MovieVO();
        vo.setId(session.getId());
        vo.setCreatorId(session.getCreatorId());
        vo.setTitle(session.getTitle());
        vo.setMovieName(session.getMovieName());
        vo.setLocation(session.getLocation());
        vo.setSessionTime(session.getSessionTime());
        vo.setMaxPlayers(session.getMaxPlayers());
        vo.setRemark(session.getRemark());
        vo.setStatus(session.getStatus());
        vo.setCreatedAt(session.getCreatedAt());
        vo.setImages(parseStringList(session.getImages()));

        SysUser creator = sysUserService.getById(session.getCreatorId());
        if (creator != null) {
            vo.setCreatorName(creator.getNickname() != null ? creator.getNickname() : creator.getUsername());
            vo.setCreatorAvatar(creator.getAvatar());
        }

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

        if (session.getResult() != null) {
            List<SettleItem> settleItems = parseResult(session.getResult());
            Map<Long, Integer> scoreMap = new HashMap<>();
            for (SettleItem item : settleItems) {
                scoreMap.put(item.getUserId(), item.getScore());
            }
            List<PlayerInfo> resultList = new ArrayList<>();
            for (PlayerInfo p : players) {
                PlayerInfo r = new PlayerInfo(p.getUserId(), p.getNickname(), p.getAvatar());
                r.setScore(scoreMap.get(p.getUserId()));
                resultList.add(r);
            }
            vo.setResult(resultList);
        }

        return vo;
    }

    private List<Long> parsePlayerIds(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<String> parseStringList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<SettleItem> parseResult(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<SettleItem>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
