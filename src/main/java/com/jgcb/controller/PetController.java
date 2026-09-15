package com.jgcb.controller;

import com.jgcb.common.Result;
import com.jgcb.dto.PetOutfitRequest;
import com.jgcb.dto.PetStatusVO;
import com.jgcb.service.PetService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/status")
    public Result<PetStatusVO> status(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(petService.getStatus(userId));
    }

    @GetMapping("/status/{userId}")
    public Result<PetStatusVO> statusOf(@PathVariable Long userId) {
        return Result.ok(petService.getStatus(userId));
    }

    @PutMapping("/outfit")
    public Result<Void> updateOutfit(@RequestBody PetOutfitRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        petService.updateOutfit(userId, request);
        return Result.ok();
    }
}
