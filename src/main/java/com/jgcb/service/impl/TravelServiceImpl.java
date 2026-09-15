package com.jgcb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgcb.dto.TravelVO;
import com.jgcb.entity.SysUser;
import com.jgcb.entity.Travel;
import com.jgcb.mapper.TravelMapper;
import com.jgcb.service.SysUserService;
import com.jgcb.service.TravelService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TravelServiceImpl extends ServiceImpl<TravelMapper, Travel> implements TravelService {

    private final SysUserService sysUserService;
    private final ObjectMapper objectMapper;

    public TravelServiceImpl(SysUserService sysUserService, ObjectMapper objectMapper) {
        this.sysUserService = sysUserService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void create(Long userId, String title, String destination, LocalDate startDate,
                       LocalDate endDate, String plan, String process, Integer participantCount, List<String> images) {
        Travel travel = new Travel();
        travel.setUserId(userId);
        travel.setTitle(title);
        travel.setDestination(destination);
        travel.setStartDate(startDate);
        travel.setEndDate(endDate);
        travel.setPlan(plan);
        travel.setProcess(process);
        travel.setParticipantCount(participantCount);
        try {
            travel.setImages(objectMapper.writeValueAsString(images));
        } catch (Exception e) {
            travel.setImages("[]");
        }
        this.save(travel);
    }

    @Override
    public Page<TravelVO> list(int page, int size) {
        Page<Travel> queryPage = new Page<>(page, size);
        LambdaQueryWrapper<Travel> wrapper = new LambdaQueryWrapper<Travel>()
                .orderByDesc(Travel::getCreatedAt);
        IPage<Travel> result = this.page(queryPage, wrapper);
        return toVOPage(result);
    }

    @Override
    public TravelVO getDetail(Long id) {
        Travel travel = this.getById(id);
        if (travel == null) return null;
        return toVO(travel);
    }

    @Override
    public void delete(Long travelId, Long userId) {
        Travel travel = this.getById(travelId);
        if (travel == null) throw new RuntimeException("旅游记录不存在");
        if (!travel.getUserId().equals(userId)) throw new RuntimeException("无权删除他人的旅游记录");
        this.removeById(travelId);
    }

    private Page<TravelVO> toVOPage(IPage<Travel> page) {
        Page<TravelVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    private TravelVO toVO(Travel travel) {
        TravelVO vo = new TravelVO();
        vo.setId(travel.getId());
        vo.setUserId(travel.getUserId());
        vo.setTitle(travel.getTitle());
        vo.setDestination(travel.getDestination());
        vo.setStartDate(travel.getStartDate());
        vo.setEndDate(travel.getEndDate());
        vo.setPlan(travel.getPlan());
        vo.setProcess(travel.getProcess());
        vo.setParticipantCount(travel.getParticipantCount());
        vo.setCreatedAt(travel.getCreatedAt());
        try {
            vo.setImages(objectMapper.readValue(travel.getImages(), new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            vo.setImages(Collections.emptyList());
        }
        SysUser user = sysUserService.getById(travel.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setAvatar(user.getAvatar());
        }
        return vo;
    }
}
