package com.example.wechatstore.modules.admin.controller;

import com.example.wechatstore.common.result.Result;
import com.example.wechatstore.modules.admin.service.AdminDashboardService;
import com.example.wechatstore.modules.admin.vo.AdminDashboardOverviewVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/overview")
    public Result<AdminDashboardOverviewVO> getOverview() {
        return Result.ok(adminDashboardService.getOverview());
    }
}
