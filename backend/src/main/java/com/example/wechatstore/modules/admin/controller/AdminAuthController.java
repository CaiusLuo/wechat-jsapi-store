package com.example.wechatstore.modules.admin.controller;

import com.example.wechatstore.common.result.Result;
import com.example.wechatstore.config.AdminAuthProperties;
import com.example.wechatstore.modules.admin.dto.AdminLoginDTO;
import com.example.wechatstore.modules.admin.vo.AdminLoginVO;
import com.example.wechatstore.utils.AdminTokenService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminAuthProperties properties;
    private final AdminTokenService adminTokenService;

    public AdminAuthController(AdminAuthProperties properties, AdminTokenService adminTokenService) {
        this.properties = properties;
        this.adminTokenService = adminTokenService;
    }

    @PostMapping("/login")
    public Result<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO dto) {
        if (!isValidCredential(dto)) {
            return Result.fail(401, "用户名或密码错误");
        }

        String token = adminTokenService.createToken(dto.username());
        return Result.ok(new AdminLoginVO(token, adminTokenService.expiresAt(token), dto.username()));
    }

    private boolean isValidCredential(AdminLoginDTO dto) {
        return StringUtils.hasText(dto.username())
                && StringUtils.hasText(dto.password())
                && dto.username().equals(properties.getUsername())
                && dto.password().equals(properties.getPassword());
    }
}
