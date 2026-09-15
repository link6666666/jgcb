package com.jgcb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgcb.dto.LoginRequest;
import com.jgcb.dto.LoginResponse;
import com.jgcb.dto.RegisterRequest;
import com.jgcb.dto.UserVO;
import com.jgcb.entity.SysUser;

import java.util.List;

public interface SysUserService extends IService<SysUser> {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    SysUser getCurrentUser(Long userId);
    List<UserVO> getUserList();
    void updatePassword(Long userId, String oldPassword, String newPassword);
    void updateAvatar(Long userId, String avatarUrl);
    void updateProfile(Long userId, String nickname, String email, String phone, String signature);
    UserVO getUserById(Long userId);
}
