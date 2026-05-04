package com.diogodev.caixa.auth.controller;

import com.diogodev.caixa.auth.dto.AuthLoginRequest;
import com.diogodev.caixa.auth.dto.AuthResponse;
import com.diogodev.caixa.auth.dto.MobileAuthResponse;
import com.diogodev.caixa.auth.dto.MobileLogoutRequest;
import com.diogodev.caixa.auth.dto.MobileRefreshRequest;
import com.diogodev.caixa.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/auth")
public class MobileAuthController {

    private final AuthService authService;

    public MobileAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<MobileAuthResponse> login(
            @Valid @RequestBody AuthLoginRequest req
    ) {
        return ResponseEntity.ok(authService.mobileLogin(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody MobileRefreshRequest req
    ) {
        return ResponseEntity.ok(authService.mobileRefresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody MobileLogoutRequest req
    ) {
        authService.mobileLogout(req.refreshToken());
        return ResponseEntity.ok().build();
    }
}