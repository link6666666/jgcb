package com.jgcb.service;

import com.jgcb.dto.PetOutfitRequest;
import com.jgcb.dto.PetStatusVO;

public interface PetService {
    PetStatusVO getStatus(Long userId);
    void updateOutfit(Long userId, PetOutfitRequest request);
}
