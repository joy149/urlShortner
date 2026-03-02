package com.jb.urlShortner.urlShortner.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jb.urlShortner.urlShortner.domain.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AuthTokenService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final String tokenSecret;
    private final long tokenTtlDays;

    public AuthTokenService(ObjectMapper objectMapper,
                            @Value("${auth.token.secret:change-this-in-production}") String tokenSecret,
                            @Value("${auth.token.ttl-days:30}") long tokenTtlDays) {
        this.objectMapper = objectMapper;
        this.tokenSecret = tokenSecret;
        this.tokenTtlDays = tokenTtlDays;
    }

    public String issueToken(AuthenticatedUser user) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("login", user.login());
            payload.put("email", user.email());
            payload.put("name", user.name());
            payload.put("exp", Instant.now().plus(tokenTtlDays, ChronoUnit.DAYS).getEpochSecond());

            String payloadJson = objectMapper.writeValueAsString(payload);
            String payloadPart = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signaturePart = sign(payloadPart);
            return payloadPart + "." + signaturePart;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to issue auth token", ex);
        }
    }

    public Optional<AuthenticatedUser> parseToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                return Optional.empty();
            }

            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                return Optional.empty();
            }

            String payloadPart = parts[0];
            String signaturePart = parts[1];
            String expectedSignature = sign(payloadPart);
            if (!MessageDigest.isEqual(signaturePart.getBytes(StandardCharsets.UTF_8),
                    expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(payloadPart);
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, MAP_TYPE);

            Object expRaw = payload.get("exp");
            long exp = expRaw instanceof Number
                    ? ((Number) expRaw).longValue()
                    : Long.parseLong(String.valueOf(expRaw));
            if (Instant.now().getEpochSecond() >= exp) {
                return Optional.empty();
            }

            String login = valueAsString(payload.get("login"));
            if (login == null || login.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new AuthenticatedUser(
                    login,
                    valueAsString(payload.get("email")),
                    valueAsString(payload.get("name"))
            ));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String sign(String payloadPart) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(key);
        byte[] signature = mac.doFinal(payloadPart.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(signature);
    }

    private String base64UrlEncode(byte[] input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }

    private String valueAsString(Object value) {
        return Objects.nonNull(value) ? String.valueOf(value) : null;
    }
}
