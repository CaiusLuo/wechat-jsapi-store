package com.example.wechatstore.modules.h5config.controller;

import com.example.wechatstore.common.result.Result;
import com.example.wechatstore.modules.h5config.dto.H5ConfigDTO;
import com.example.wechatstore.modules.h5config.service.H5ConfigService;
import com.example.wechatstore.modules.h5config.vo.H5ConfigVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/h5-config")
public class AdminH5ConfigController {

    private final H5ConfigService h5ConfigService;

    public AdminH5ConfigController(H5ConfigService h5ConfigService) {
        this.h5ConfigService = h5ConfigService;
    }

    @GetMapping
    public Result<H5ConfigVO> getConfig() {
        return Result.ok(h5ConfigService.getConfig());
    }

    @PutMapping
    public Result<H5ConfigVO> updateConfig(@RequestBody H5ConfigDTO dto) {
        return Result.ok(h5ConfigService.updateConfig(dto));
    }
}
