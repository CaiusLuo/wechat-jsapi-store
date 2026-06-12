package com.example.wechatstore.modules.h5config.controller;

import com.example.wechatstore.common.result.Result;
import com.example.wechatstore.modules.h5config.service.H5ConfigService;
import com.example.wechatstore.modules.h5config.vo.H5ConfigVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/h5/config")
public class H5ConfigController {

    private final H5ConfigService h5ConfigService;

    public H5ConfigController(H5ConfigService h5ConfigService) {
        this.h5ConfigService = h5ConfigService;
    }

    @GetMapping
    public Result<H5ConfigVO> getConfig() {
        return Result.ok(h5ConfigService.getConfig());
    }
}
