package com.example.wechatstore.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.wechatstore.common.exception.UnauthorizedException;
import com.example.wechatstore.utils.AdminTokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AdminAuthProperties properties;
    private final AdminTokenService adminTokenService;

    public AdminTokenAuthenticationFilter(AdminAuthProperties properties, AdminTokenService adminTokenService) {
        this.properties = properties;
        this.adminTokenService = adminTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isAdminRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestToken = resolveToken(request);
        try {
            AdminTokenService.AdminPrincipal principal = adminTokenService.parseToken(requestToken);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal.username(),
                    null,
                    AuthorityUtils.createAuthorityList("ROLE_ADMIN")
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UnauthorizedException ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        return request.getHeader(properties.getHeaderName());
    }

    private boolean isAdminRequest(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String adminPrefix = (contextPath == null ? "" : contextPath) + "/api/admin/";
        return uri.startsWith(adminPrefix);
    }
}
