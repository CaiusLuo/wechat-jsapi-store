package com.example.wechatstore.modules.user.controller;

import com.example.wechatstore.common.result.Result;
import com.example.wechatstore.modules.user.dto.UpdateUserProfileDTO;
import com.example.wechatstore.modules.user.service.WxUserService;
import com.example.wechatstore.modules.user.vo.UserProfileVO;
import com.example.wechatstore.utils.H5AuthResolver;
import com.example.wechatstore.utils.H5TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/h5/user/profile")
public class H5UserProfileController {

    private final H5AuthResolver h5AuthResolver;
    private final WxUserService wxUserService;

    public H5UserProfileController(H5AuthResolver h5AuthResolver, WxUserService wxUserService) {
        this.h5AuthResolver = h5AuthResolver;
        this.wxUserService = wxUserService;
    }

    @GetMapping
    public Result<UserProfileVO> getProfile(HttpServletRequest request) {
        H5TokenService.H5UserPrincipal user = h5AuthResolver.requireUser(request);
        return Result.ok(wxUserService.getProfile(user.userId(), user.openid()));
    }

    @PutMapping
    public Result<UserProfileVO> updateProfile(
            @Valid @RequestBody UpdateUserProfileDTO dto,
            HttpServletRequest request
    ) {
        H5TokenService.H5UserPrincipal user = h5AuthResolver.requireUser(request);
        return Result.ok(wxUserService.updateProfile(user.userId(), user.openid(), dto));
    }
}
