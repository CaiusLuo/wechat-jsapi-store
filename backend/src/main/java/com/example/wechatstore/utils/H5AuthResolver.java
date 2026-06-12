package com.example.wechatstore.utils;

import com.example.wechatstore.common.exception.UnauthorizedException;
import com.example.wechatstore.config.H5AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Component
public class H5AuthResolver {

    private final H5AuthProperties properties;
    private final H5TokenService tokenService;

    public H5AuthResolver(H5AuthProperties properties, H5TokenService tokenService) {
        this.properties = properties;
        this.tokenService = tokenService;
    }

    public H5TokenService.H5UserPrincipal requireUser(HttpServletRequest request) {
        String token = resolveBearerToken(request);
        if (!StringUtils.hasText(token)) {
            token = resolveCookieToken(request);
        }
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("h5 login required");
        }
        return tokenService.parseToken(token);
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    private String resolveCookieToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> properties.getCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
