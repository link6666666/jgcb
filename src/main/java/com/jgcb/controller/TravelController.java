package com.jgcb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgcb.common.Result;
import com.jgcb.dto.TravelRequest;
import com.jgcb.dto.TravelVO;
import com.jgcb.service.TravelService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/travels")
public class TravelController {

    private final TravelService travelService;

    public TravelController(TravelService travelService) {
        this.travelService = travelService;
    }

    @PostMapping
    public Result<Void> create(@RequestBody TravelRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        travelService.create(userId, request.getTitle(), request.getDestination(),
                request.getStartDate(), request.getEndDate(),
                request.getPlan(), request.getProcess(), request.getParticipantCount(), request.getImages());
        return Result.ok();
    }

    @GetMapping
    public Result<Page<TravelVO>> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return Result.ok(travelService.list(page, size));
    }

    @GetMapping("/{id}")
    public Result<TravelVO> detail(@PathVariable Long id) {
        TravelVO vo = travelService.getDetail(id);
        if (vo == null) return Result.fail("旅游记录不存在");
        return Result.ok(vo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        travelService.delete(id, userId);
        return Result.ok();
    }
}
