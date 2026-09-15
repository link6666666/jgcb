package com.jgcb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jgcb.dto.PetDecorationVO;
import com.jgcb.dto.PetOutfitRequest;
import com.jgcb.dto.PetStatusVO;
import com.jgcb.entity.PetOutfit;
import com.jgcb.entity.SysUser;
import com.jgcb.mapper.PetOutfitMapper;
import com.jgcb.mapper.SysUserMapper;
import com.jgcb.service.PetService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PetServiceImpl implements PetService {

    private final PetOutfitMapper petOutfitMapper;
    private final SysUserMapper sysUserMapper;

    public PetServiceImpl(PetOutfitMapper petOutfitMapper, SysUserMapper sysUserMapper) {
        this.petOutfitMapper = petOutfitMapper;
        this.sysUserMapper = sysUserMapper;
    }

    private static final List<PetDecorationVO> DECORATIONS = new ArrayList<>();

    static {
        DECORATIONS.add(new PetDecorationVO("top_hat", "head", "🎩", "礼帽", 2));
        DECORATIONS.add(new PetDecorationVO("cap", "head", "🧢", "棒球帽", 3));
        DECORATIONS.add(new PetDecorationVO("graduate", "head", "🎓", "学士帽", 4));
        DECORATIONS.add(new PetDecorationVO("crown", "head", "👑", "皇冠", 5));

        DECORATIONS.add(new PetDecorationVO("scarf", "neck", "🧣", "围巾", 2));
        DECORATIONS.add(new PetDecorationVO("bow", "neck", "🎀", "蝴蝶结", 4));

        DECORATIONS.add(new PetDecorationVO("shirt", "body", "👕", "T恤", 3));
        DECORATIONS.add(new PetDecorationVO("vest", "body", "🦺", "背心", 4));
        DECORATIONS.add(new PetDecorationVO("coat", "body", "🧥", "外套", 5));

        DECORATIONS.add(new PetDecorationVO("wand", "accessory", "🪄", "魔法棒", 3));
        DECORATIONS.add(new PetDecorationVO("rifle", "accessory", "🔫", "步枪", 5));
    }

    private static int levelOf(Integer exp) {
        int e = exp == null ? 0 : exp;
        return Math.min(e / 100 + 1, 6);
    }

    @Override
    public PetStatusVO getStatus(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        int level = levelOf(user == null ? null : user.getExp());

        PetOutfit outfit = petOutfitMapper.selectOne(new LambdaQueryWrapper<PetOutfit>()
                .eq(PetOutfit::getUserId, userId));

        PetStatusVO vo = new PetStatusVO();
        vo.setLevel(level);
        vo.setExp(user == null ? 0 : (user.getExp() == null ? 0 : user.getExp()));
        vo.setDecorations(DECORATIONS);
        if (outfit != null) {
            vo.setHead(outfit.getHead());
            vo.setNeck(outfit.getNeck());
            vo.setBody(outfit.getBody());
            vo.setAccessory(outfit.getAccessory());
        }
        return vo;
    }

    @Override
    public void updateOutfit(Long userId, PetOutfitRequest request) {
        SysUser user = sysUserMapper.selectById(userId);
        int level = levelOf(user == null ? null : user.getExp());

        String head = validate("head", request.getHead(), level);
        String neck = validate("neck", request.getNeck(), level);
        String body = validate("body", request.getBody(), level);
        String accessory = validate("accessory", request.getAccessory(), level);

        PetOutfit outfit = petOutfitMapper.selectOne(new LambdaQueryWrapper<PetOutfit>()
                .eq(PetOutfit::getUserId, userId));
        if (outfit == null) {
            outfit = new PetOutfit();
            outfit.setUserId(userId);
            outfit.setHead(head);
            outfit.setNeck(neck);
            outfit.setBody(body);
            outfit.setAccessory(accessory);
            outfit.setUpdatedAt(LocalDateTime.now());
            petOutfitMapper.insert(outfit);
        } else {
            LambdaUpdateWrapper<PetOutfit> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PetOutfit::getUserId, userId)
                    .set(PetOutfit::getHead, head)
                    .set(PetOutfit::getNeck, neck)
                    .set(PetOutfit::getBody, body)
                    .set(PetOutfit::getAccessory, accessory)
                    .set(PetOutfit::getUpdatedAt, LocalDateTime.now());
            petOutfitMapper.update(null, wrapper);
        }
    }

    private String validate(String slot, String code, int level) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (PetDecorationVO d : DECORATIONS) {
            if (d.getSlot().equals(slot) && d.getCode().equals(code)) {
                if (d.getUnlockLevel() <= level) {
                    return code;
                }
                throw new RuntimeException("该装饰尚未解锁");
            }
        }
        throw new RuntimeException("无效的装饰");
    }
}
