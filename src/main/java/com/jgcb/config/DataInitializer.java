package com.jgcb.config;

import com.jgcb.entity.SysUser;
import com.jgcb.service.SysUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysUserService sysUserService, PasswordEncoder passwordEncoder) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        SysUser admin = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, "admin").one();
        if (admin == null) {
            SysUser user = new SysUser();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setNickname("管理员");
            user.setStatus(1);
            sysUserService.save(user);
            System.out.println(">>> 管理员账号已初始化: admin / 123456");
        }
    }
}
