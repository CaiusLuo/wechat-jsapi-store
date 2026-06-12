package com.example.wechatstore.utils;

import com.example.wechatstore.common.exception.UnauthorizedException;
import com.example.wechatstore.config.H5AuthProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class H5TokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final H5AuthProperties properties;

    public H5TokenService(H5AuthProperties properties) {
        this.properties = properties;
    }

    public String createToken(Long userId, String openid) {
        long expiresAt = Instant.now().getEpochSecond() + properties.getTtlSeconds();
        String payload = userId + "|" + openid + "|" + expiresAt;
        String signed = payload + "|" + sign(payload);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(signed.getBytes(StandardCharsets.UTF_8));
    }

    public H5UserPrincipal parseToken(String token) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|");
            if (parts.length != 4) {
                throw new UnauthorizedException("invalid h5 token");
            }

            String payload = parts[0] + "|" + parts[1] + "|" + parts[2];
            String expected = sign(payload);
            if (!constantTimeEquals(expected, parts[3])) {
                throw new UnauthorizedException("invalid h5 token signature");
            }

            long expiresAt = Long.parseLong(parts[2]);
            if (expiresAt < Instant.now().getEpochSecond()) {
                throw new UnauthorizedException("h5 token expired");
            }

            return new H5UserPrincipal(Long.parseLong(parts[0]), parts[1], expiresAt);
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedException("invalid h5 token");
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.getTokenSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("failed to sign h5 token", ex);
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

    public record H5UserPrincipal(Long userId, String openid, long expiresAt) {
    }
}
