package com.example.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenUtil {

    private static final long EXPIRE_SECONDS = 3600;

    private final String secret;

    public AuthTokenUtil(@Value("${app.auth.token-secret:library-demo-secret-change-me}") String secret) {
        this.secret = secret;
    }

    public String generateToken(int userId, String phoneNumber) {
        long exp = Instant.now().getEpochSecond() + EXPIRE_SECONDS;
        String payload = userId + ":" + phoneNumber + ":" + exp;
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(encodedPayload);
        return encodedPayload + "." + signature;
    }

    public Integer validateAndGetUserId(String token) {
        if (token == null || token.isBlank() || !token.contains(".")) {
            return null;
        }

        String[] parts = token.split("\\.", 2);
        String payloadPart = parts[0];
        String signaturePart = parts[1];

        String expectedSignature = sign(payloadPart);
        if (!MessageDigest.isEqual(signaturePart.getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }

        String payload = new String(Base64.getUrlDecoder().decode(payloadPart), StandardCharsets.UTF_8);
        String[] fields = payload.split(":", 3);
        if (fields.length != 3) {
            return null;
        }

        int userId;
        long exp;
        try {
            userId = Integer.parseInt(fields[0]);
            exp = Long.parseLong(fields[2]);
        } catch (NumberFormatException ex) {
            return null;
        }

        if (Instant.now().getEpochSecond() > exp) {
            return null;
        }

        return userId;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign auth token", ex);
        }
    }
}
