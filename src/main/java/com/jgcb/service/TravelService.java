package com.jgcb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgcb.dto.TravelVO;
import com.jgcb.entity.Travel;

import java.time.LocalDate;
import java.util.List;

public interface TravelService extends IService<Travel> {
    void create(Long userId, String title, String destination, LocalDate startDate,
                LocalDate endDate, String plan, String process, Integer participantCount, List<String> images);
    Page<TravelVO> list(int page, int size);
    TravelVO getDetail(Long id);
    void delete(Long travelId, Long userId);
}
