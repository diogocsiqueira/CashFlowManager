package com.diogodev.caixa.auth.controller;

import com.diogodev.caixa.auth.dto.AuthLoginRequest;
import com.diogodev.caixa.auth.dto.AuthRegisterRequest;
import com.diogodev.caixa.auth.dto.AuthResponse;
import com.diogodev.caixa.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${app.cookies.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookies.sameSite:Lax}")
    private String cookieSameSite;

    // cookie expira junto do refresh token (em dias)
    @Value("${app.cookies.refreshMaxAgeDays:14}")
    private int refreshMaxAgeDays;

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody AuthRegisterRequest req) {
        authService.register(req);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthLoginRequest req,
            HttpServletResponse res
    ) {
        AuthResponse out = authService.login(req, cookieWriter(res));
        return ResponseEntity.ok(out);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refresh,
            HttpServletResponse res
    ) {
        AuthResponse out = authService.refresh(refresh, cookieWriter(res));
        return ResponseEntity.ok(out);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refresh,
            HttpServletResponse res
    ) {
        authService.logout(refresh, cookieWriter(res));
        return ResponseEntity.ok().build();
    }


    private AuthService.CookieWriter cookieWriter(HttpServletResponse res) {
        return new AuthService.CookieWriter() {

            private final DateTimeFormatter RFC_1123 =
                    DateTimeFormatter.RFC_1123_DATE_TIME;

            private void writeCookie(String value, int maxAgeSeconds) {
                String secure = cookieSecure ? "; Secure" : "";

                // Expires ajuda compatibilidade e deixa explícito no browser
                String expires = "; Expires=" + ZonedDateTime.now(ZoneOffset.UTC)
                        .plusSeconds(Math.max(0, maxAgeSeconds))
                        .format(RFC_1123);

                res.addHeader("Set-Cookie",
                        "refresh_token=" + value +
                                "; Path=/api/auth; HttpOnly" +
                                secure +
                                "; SameSite=" + cookieSameSite +
                                "; Max-Age=" + maxAgeSeconds +
                                expires
                );
            }

            @Override
            public void setRefreshCookie(String refreshRaw) {
                int maxAge = Math.max(1, refreshMaxAgeDays) * 24 * 60 * 60;
                writeCookie(refreshRaw, maxAge);
            }

            @Override
            public void clearRefreshCookie() {
                writeCookie("", 0);
            }
        };
    }
}
