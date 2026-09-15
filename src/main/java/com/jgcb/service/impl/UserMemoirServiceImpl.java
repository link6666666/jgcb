package com.jgcb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgcb.dto.MemoirVO;
import com.jgcb.entity.SysUser;
import com.jgcb.entity.UserMemoir;
import com.jgcb.mapper.UserMemoirMapper;
import com.jgcb.service.MemoirCommentService;
import com.jgcb.service.MemoirLikeService;
import com.jgcb.service.SysUserService;
import com.jgcb.service.UserMemoirService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserMemoirServiceImpl extends ServiceImpl<UserMemoirMapper, UserMemoir> implements UserMemoirService {

    private final SysUserService sysUserService;
    private final ObjectMapper objectMapper;
    private final MemoirLikeService memoirLikeService;
    private final MemoirCommentService memoirCommentService;

    public UserMemoirServiceImpl(SysUserService sysUserService, ObjectMapper objectMapper,
                                 MemoirLikeService memoirLikeService, MemoirCommentService memoirCommentService) {
        this.sysUserService = sysUserService;
        this.objectMapper = objectMapper;
        this.memoirLikeService = memoirLikeService;
        this.memoirCommentService = memoirCommentService;
    }

    @Override
    public void create(Long userId, String title, String content, List<String> images) {
        UserMemoir memoir = new UserMemoir();
        memoir.setUserId(userId);
        memoir.setTitle(title);
        memoir.setContent(content);
        try {
            memoir.setImages(images != null ? objectMapper.writeValueAsString(images) : null);
        } catch (JsonProcessingException e) {
            memoir.setImages(null);
        }
        this.save(memoir);
    }

    @Override
    public Page<MemoirVO> list(int page, int size) {
        Page<UserMemoir> memoirPage = this.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserMemoir>().orderByDesc(UserMemoir::getCreatedAt)
        );
        return toMemoirVOPage(memoirPage, null);
    }

    @Override
    public Page<MemoirVO> listByUserId(Long userId, int page, int size) {
        Page<UserMemoir> memoirPage = this.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserMemoir>()
                        .eq(UserMemoir::getUserId, userId)
                        .orderByDesc(UserMemoir::getCreatedAt)
        );
        return toMemoirVOPage(memoirPage, null);
    }

    @Override
    public MemoirVO getDetail(Long id, Long currentUserId) {
        UserMemoir memoir = this.getById(id);
        if (memoir == null) return null;
        return toMemoirVO(memoir, currentUserId);
    }

    @Override
    public void delete(Long memoirId, Long userId) {
        UserMemoir memoir = this.getById(memoirId);
        if (memoir == null) {
            throw new RuntimeException("回忆录不存在");
        }
        if (!memoir.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人的回忆录");
        }
        this.removeById(memoirId);
    }

    private Page<MemoirVO> toMemoirVOPage(IPage<UserMemoir> page, Long currentUserId) {
        Page<MemoirVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(m -> toMemoirVO(m, currentUserId))
                .collect(Collectors.toList()));
        return voPage;
    }

    private MemoirVO toMemoirVO(UserMemoir memoir, Long currentUserId) {
        MemoirVO vo = new MemoirVO();
        vo.setId(memoir.getId());
        vo.setUserId(memoir.getUserId());
        vo.setTitle(memoir.getTitle());
        vo.setContent(memoir.getContent());
        vo.setCreatedAt(memoir.getCreatedAt());
        try {
            vo.setImages(objectMapper.readValue(memoir.getImages(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
        } catch (Exception e) {
            vo.setImages(Collections.emptyList());
        }
        SysUser user = sysUserService.getById(memoir.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setAvatar(user.getAvatar());
        }
        vo.setLikeCount(memoirLikeService.countLikes(memoir.getId()));
        vo.setCommentCount(memoirCommentService.countComments(memoir.getId()));
        if (currentUserId != null) {
            vo.setLiked(memoirLikeService.isLiked(currentUserId, memoir.getId()));
        }
        return vo;
    }
}
