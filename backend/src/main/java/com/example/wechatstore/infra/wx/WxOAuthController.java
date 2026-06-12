package com.example.wechatstore.infra.wx;

import com.example.wechatstore.common.exception.BizException;
import com.example.wechatstore.common.result.Result;
import com.example.wechatstore.config.H5AuthProperties;
import com.example.wechatstore.config.WxMpConfig;
import com.example.wechatstore.modules.user.entity.WxUser;
import com.example.wechatstore.modules.user.service.WxUserService;
import com.example.wechatstore.modules.user.vo.H5LoginVO;
import com.example.wechatstore.utils.H5AuthResolver;
import com.example.wechatstore.utils.H5TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/wx/oauth")
public class WxOAuthController {

    private final WxMpConfig wxMpConfig;
    private final WxMpService wxMpService;
    private final WxUserService wxUserService;
    private final H5TokenService h5TokenService;
    private final H5AuthResolver h5AuthResolver;
    private final H5AuthProperties h5AuthProperties;

    public WxOAuthController(
            WxMpConfig wxMpConfig,
            WxMpService wxMpService,
            WxUserService wxUserService,
            H5TokenService h5TokenService,
            H5AuthResolver h5AuthResolver,
            H5AuthProperties h5AuthProperties
    ) {
        this.wxMpConfig = wxMpConfig;
        this.wxMpService = wxMpService;
        this.wxUserService = wxUserService;
        this.h5TokenService = h5TokenService;
        this.h5AuthResolver = h5AuthResolver;
        this.h5AuthProperties = h5AuthProperties;
    }

    /**
     * 构建微信网页授权地址。
     * <p>
     * 说明：前端先拿这个地址跳转到微信侧，用户授权后会回到配置的回调地址。
     * </p>
     *
     * @param redirectUri 授权完成后的回跳地址；为空时使用配置中的默认回调地址
     * @param state       状态标识，用于区分来源页面，默认值为 h5
     * @param scope       授权范围，默认拉取微信昵称与头像
     * @return 微信网页授权跳转链接
     */
    @GetMapping("/url")
    public Result<String> buildOAuthUrl(
            @RequestParam(required = false) String redirectUri,
            @RequestParam(required = false, defaultValue = "h5") String state,
            @RequestParam(required = false, defaultValue = "snsapi_userinfo") String scope
    ) {
        String target = StringUtils.hasText(redirectUri) ? redirectUri : wxMpConfig.getOauthCallbackUrl();
        return Result.ok(wxMpService.getOAuth2Service()
                .buildAuthorizationUrl(target, normalizeOAuthScope(scope), state));
    }

    /**
     * 微信授权回调入口。
     * <p>
     * 核心流程：用 code 换取 openid -> 尝试读取微信资料 -> 创建或查询本地用户 -> 签发 H5 token -> 写入 Cookie。
     * </p>
     *
     * @param code     微信回调携带的授权 code
     * @param response 响应对象，用于设置登录态 Cookie
     * @return H5 登录结果，包含 token 和过期时间
     */
    @GetMapping("/callback")
    public Result<H5LoginVO> callback(
            @RequestParam String code,
            HttpServletResponse response
    ) {
        try {
            WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
            if (!StringUtils.hasText(accessToken.getOpenId())) {
                throw new BizException("wechat oauth did not return openid");
            }

            WxOAuth2UserInfo userInfo = fetchUserInfoSafely(accessToken);
            WxUser user = wxUserService.findOrCreateByOpenid(
                    accessToken.getOpenId(),
                    userInfo == null ? null : userInfo.getNickname(),
                    userInfo == null ? null : userInfo.getHeadImgUrl()
            );
            String token = h5TokenService.createToken(user.getId(), user.getOpenid());
            long expiresAt = h5TokenService.parseToken(token).expiresAt();
            response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(token).toString());
            return Result.ok(new H5LoginVO(token, expiresAt));
        } catch (WxErrorException ex) {
            throw new BizException("wechat oauth failed: " + ex.getMessage());
        }
    }

    private String normalizeOAuthScope(String scope) {
        if (WxConsts.OAuth2Scope.SNSAPI_BASE.equals(scope)) {
            return WxConsts.OAuth2Scope.SNSAPI_BASE;
        }
        return WxConsts.OAuth2Scope.SNSAPI_USERINFO;
    }

    private WxOAuth2UserInfo fetchUserInfoSafely(WxOAuth2AccessToken accessToken) {
        if (!StringUtils.hasText(accessToken.getAccessToken())) {
            return null;
        }
        try {
            return wxMpService.getOAuth2Service().getUserInfo(accessToken, "zh_CN");
        } catch (WxErrorException ex) {
            log.warn("wechat oauth userinfo failed: {}", ex.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * 获取当前 H5 登录用户信息。
     * <p>
     * 这里不重新登录，只校验请求里的 token 是否有效，并返回 token 过期时间。
     * </p>
     *
     * @param request 当前请求
     * @return 当前登录态信息
     */
    @GetMapping("/me")
    public Result<H5LoginVO> me(HttpServletRequest request) {
        H5TokenService.H5UserPrincipal user = h5AuthResolver.requireUser(request);
        return Result.ok(new H5LoginVO(null, user.expiresAt()));
    }

    /**
     * 构建 H5 登录 Cookie。
     * <p>
     * 关键点：HttpOnly 可降低前端脚本读取风险，SameSite=Lax 可缓解部分跨站请求场景。
     * </p>
     */
    private ResponseCookie buildCookie(String token) {
        return ResponseCookie.from(h5AuthProperties.getCookieName(), token)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(h5AuthProperties.getTtlSeconds())
                .build();
    }
}
