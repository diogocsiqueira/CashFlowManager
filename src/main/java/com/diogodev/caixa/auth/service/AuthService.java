package com.diogodev.caixa.auth.service;

import com.diogodev.caixa.auth.dto.AuthLoginRequest;
import com.diogodev.caixa.auth.dto.AuthMeResponse;
import com.diogodev.caixa.auth.dto.AuthRegisterRequest;
import com.diogodev.caixa.auth.dto.AuthResponse;
import com.diogodev.caixa.core.user.domain.enums.Role;
import com.diogodev.caixa.auth.domain.model.RefreshToken;
import com.diogodev.caixa.core.user.domain.model.User;
import com.diogodev.caixa.auth.repository.RefreshTokenRepository;
import com.diogodev.caixa.core.user.repository.UserRepository;
import com.diogodev.caixa.shared.exception.UnauthorizedException;
import com.diogodev.caixa.auth.dto.MobileAuthResponse;
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
                .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Usuário desativado");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciais inválidas");
        }

        String access = tokenService.generateAccessToken(user);
        String refreshRaw = tokenService.generateAndStoreRefreshToken(user, req.deviceId());

        cookieWriter.setRefreshCookie(refreshRaw);

        return new AuthResponse(access);
    }

    @Transactional
    public AuthResponse refresh(String refreshRaw, CookieWriter cookieWriter) {
        if (refreshRaw == null || refreshRaw.isBlank()) {
            throw new UnauthorizedException("Sem refresh token");
        }

        String hash = tokenService.sha256Hex(refreshRaw);

        RefreshToken rt = refreshTokenRepository.findValidWithUserAndRoles(hash)
                .orElseThrow(() -> new UnauthorizedException("Refresh inválido"));

        if (rt.getExpiresAt().isBefore(Instant.now())) {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            cookieWriter.clearRefreshCookie();

            throw new UnauthorizedException("Refresh expirado");
        }

        User user = rt.getUser();

        String access = tokenService.generateAccessToken(user);

        return new AuthResponse(access);
    }

    public MobileAuthResponse mobileLogin(AuthLoginRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.email().trim())
                .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Usuário desativado");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciais inválidas");
        }

        String access = tokenService.generateAccessToken(user);
        String refreshRaw = tokenService.generateAndStoreRefreshToken(user, "mobile");

        return new MobileAuthResponse(access, refreshRaw);
    }

    @Transactional
    public AuthResponse mobileRefresh(String refreshRaw) {
        if (refreshRaw == null || refreshRaw.isBlank()) {
            throw new UnauthorizedException("Sem refresh token");
        }

        String hash = tokenService.sha256Hex(refreshRaw);

        RefreshToken rt = refreshTokenRepository.findValidWithUserAndRoles(hash)
                .orElseThrow(() -> new UnauthorizedException("Refresh inválido"));

        if (rt.getExpiresAt().isBefore(Instant.now())) {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            throw new UnauthorizedException("Refresh expirado");
        }

        User user = rt.getUser();
        String access = tokenService.generateAccessToken(user);

        return new AuthResponse(access);
    }

    public void mobileLogout(String refreshRaw) {
        if (refreshRaw == null || refreshRaw.isBlank()) return;

        String hash = tokenService.sha256Hex(refreshRaw);

        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
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

    public AuthMeResponse me(User user) {
        return new AuthMeResponse(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
        );
    }

    public User loadUserByPrincipalName(String principalName) {
        if (principalName == null || principalName.isBlank()) {
            throw new UnauthorizedException("Usuário não autenticado");
        }

        if (principalName.matches("\\d+")) {
            Long id = Long.parseLong(principalName);

            return userRepository.findById(id)
                    .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
        }

        return userRepository.findByEmailIgnoreCase(principalName)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
    }

    public interface CookieWriter {
        void setRefreshCookie(String refreshRaw);
        void clearRefreshCookie();
    }
}