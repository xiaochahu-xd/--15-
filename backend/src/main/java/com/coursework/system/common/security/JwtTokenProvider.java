package com.coursework.system.common.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    public JwtTokenProvider(JwtProperties jwtProperties, ObjectMapper objectMapper) {
        this.jwtProperties = jwtProperties;
        this.objectMapper = objectMapper;
    }

    public String createToken(UserPrincipal principal) {
        long now = Instant.now().getEpochSecond();
        Map<String, Object> header = new HashMap<String, Object>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("sub", String.valueOf(principal.getId()));
        payload.put("username", principal.getUsername());
        payload.put("realName", principal.getRealName());
        payload.put("roles", principal.getRoles());
        payload.put("iat", now);
        payload.put("exp", now + jwtProperties.getExpirationMinutes() * 60);

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public JwtUserInfo parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("JWT 格式不正确");
            }
            String unsignedToken = parts[0] + "." + parts[1];
            String expected = sign(unsignedToken);
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("JWT 签名无效");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {
            });
            Number exp = (Number) payload.get("exp");
            if (exp == null || Instant.now().getEpochSecond() > exp.longValue()) {
                throw new IllegalArgumentException("JWT 已过期");
            }

            Long userId = Long.valueOf(String.valueOf(payload.get("sub")));
            String username = String.valueOf(payload.get("username"));
            String realName = String.valueOf(payload.get("realName"));
            List<String> roles = new ArrayList<String>();
            Object roleObject = payload.get("roles");
            if (roleObject instanceof List) {
                for (Object role : (List<?>) roleObject) {
                    roles.add(String.valueOf(role));
                }
            }
            return new JwtUserInfo(userId, username, realName, roles);
        } catch (Exception exception) {
            throw new IllegalArgumentException("JWT 校验失败：" + exception.getMessage(), exception);
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 生成失败", exception);
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 签名失败", exception);
        }
    }

    public static class JwtUserInfo {
        private final Long userId;
        private final String username;
        private final String realName;
        private final List<String> roles;

        public JwtUserInfo(Long userId, String username, String realName, List<String> roles) {
            this.userId = userId;
            this.username = username;
            this.realName = realName;
            this.roles = roles;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getRealName() {
            return realName;
        }

        public List<String> getRoles() {
            return roles;
        }
    }
}
