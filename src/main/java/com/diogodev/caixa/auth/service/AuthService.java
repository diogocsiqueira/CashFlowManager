package com.diogodev.caixa.auth.service;

import com.diogodev.caixa.auth.dto.AuthLoginRequest;
import com.diogodev.caixa.auth.dto.AuthRegisterRequest;
import com.diogodev.caixa.auth.dto.AuthResponse;
import com.diogodev.caixa.core.user.domain.enums.Role;
import com.diogodev.caixa.auth.domain.model.RefreshToken;
import com.diogodev.caixa.core.user.domain.model.User;
import com.diogodev.caixa.auth.repository.RefreshTokenRepository;
import com.diogodev.caixa.core.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public void register(AuthRegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        User u = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .enabled(true)
                .build();
        u.getRoles().add(Role.USER);

        userRepository.save(u);
    }

    public AuthResponse login(AuthLoginRequest req, CookieWriter cookieWriter) {
        User user = userRepository.findByEmailIgnoreCase(req.email().trim())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!user.isEnabled()) throw new IllegalArgumentException("Usuário desativado");

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        String access = tokenService.generateAccessToken(user);
        String refreshRaw = tokenService.generateAndStoreRefreshToken(user, req.deviceId());

        cookieWriter.setRefreshCookie(refreshRaw);

        return new AuthResponse(access);
    }

    @Transactional
    public AuthResponse refresh(String refreshRaw, CookieWriter cookieWriter) {
        if (refreshRaw == null || refreshRaw.isBlank()) {
            throw new IllegalArgumentException("Sem refresh token");
        }

        String hash = tokenService.sha256Hex(refreshRaw);

        RefreshToken rt = refreshTokenRepository.findValidWithUserAndRoles(hash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh inválido"));


        if (rt.getExpiresAt().isBefore(Instant.now())) {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            throw new IllegalArgumentException("Refresh expirado");
        }

        User user = rt.getUser();

        // MVP: mantém o refresh atual (sem rotação)
        // Melhor: rotacionar (revoga rt e cria outro). A gente faz depois se quiser.

        String access = tokenService.generateAccessToken(user);
        return new AuthResponse(access);
    }

    public void logout(String refreshRaw, CookieWriter cookieWriter) {
        cookieWriter.clearRefreshCookie();

        if (refreshRaw == null || refreshRaw.isBlank()) return;

        String hash = tokenService.sha256Hex(refreshRaw);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    public interface CookieWriter {
        void setRefreshCookie(String refreshRaw);
        void clearRefreshCookie();
    }
}
