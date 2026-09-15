package com.jgcb.controller;

import com.jgcb.common.Result;
import com.jgcb.dto.UserVO;
import com.jgcb.entity.SysUser;
import com.jgcb.service.SysUserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final SysUserService sysUserService;

    public UserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping("/list")
    public Result<List<UserVO>> list() {
        return Result.ok(sysUserService.getUserList());
    }

    @GetMapping("/info")
    public Result<SysUser> info(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(sysUserService.getCurrentUser(userId));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> params,
                                        Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        sysUserService.updatePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return Result.ok();
    }

    @PutMapping("/avatar")
    public Result<Void> updateAvatar(@RequestBody Map<String, String> params,
                                      Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        sysUserService.updateAvatar(userId, params.get("avatarUrl"));
        return Result.ok();
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> params,
                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        sysUserService.updateProfile(userId, params.get("nickname"), params.get("email"), params.get("phone"), params.get("signature"));
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        return Result.ok(sysUserService.getUserById(id));
    }
}
