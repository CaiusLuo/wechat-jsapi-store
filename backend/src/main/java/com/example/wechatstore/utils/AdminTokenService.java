package com.example.wechatstore.utils;

import com.example.wechatstore.common.exception.UnauthorizedException;
import com.example.wechatstore.config.AdminAuthProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AdminTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final AdminAuthProperties properties;

    public AdminTokenService(AdminAuthProperties properties) {
        this.properties = properties;
    }

    public String createToken(String username) {
        long expiresAt = Instant.now().getEpochSecond() + properties.getTtlSeconds();
        String payload = username + "|" + expiresAt;
        String signed = payload + "|" + sign(payload);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(signed.getBytes(StandardCharsets.UTF_8));
    }

    public AdminPrincipal parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("admin login required");
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|");
            if (parts.length != 3) {
                throw new UnauthorizedException("invalid admin token");
            }

            String payload = parts[0] + "|" + parts[1];
            if (!constantTimeEquals(sign(payload), parts[2])) {
                throw new UnauthorizedException("invalid admin token signature");
            }

            long expiresAt = Long.parseLong(parts[1]);
            if (expiresAt < Instant.now().getEpochSecond()) {
                throw new UnauthorizedException("admin token expired");
            }
            return new AdminPrincipal(parts[0], expiresAt);
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedException("invalid admin token");
        }
    }

    public long expiresAt(String token) {
        return parseToken(token).expiresAt();
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.getTokenSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("failed to sign admin token", ex);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left.length() != right.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length(); i++) {
            result |= left.charAt(i) ^ right.charAt(i);
        }
        return result == 0;
    }

    public record AdminPrincipal(String username, long expiresAt) {
    }
}
