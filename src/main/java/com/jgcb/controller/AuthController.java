package com.jgcb.controller;

import com.jgcb.common.Result;
import com.jgcb.dto.LoginRequest;
import com.jgcb.dto.LoginResponse;
import com.jgcb.dto.RegisterRequest;
import com.jgcb.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserService sysUserService;

    public AuthController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = sysUserService.login(request);
        return Result.ok(response);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        sysUserService.register(request);
        return Result.ok();
    }
}
