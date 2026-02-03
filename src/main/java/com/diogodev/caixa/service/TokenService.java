package com.diogodev.caixa.service;

import com.diogodev.caixa.domain.enums.Role;
import com.diogodev.caixa.domain.model.RefreshToken;
import com.diogodev.caixa.domain.model.User;
import com.diogodev.caixa.repository.RefreshTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

@Service
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.accessMinutes}")
    private long accessMinutes;

    @Value("${app.jwt.refreshDays}")
    private long refreshDays;

    public TokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessMinutes, ChronoUnit.MINUTES);

        Set<String> roles = user.getRoles().stream().map(Role::name).collect(java.util.stream.Collectors.toSet());

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public String generateAndStoreRefreshToken(User user, String deviceId) {
        String raw = java.util.UUID.randomUUID() + "." + java.util.UUID.randomUUID();
        String hash = sha256Hex(raw);

        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .deviceId(deviceId == null || deviceId.isBlank() ? "web" : deviceId)
                .expiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(rt);
        return raw;
    }

    public String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 failed", e);
        }
    }
}
