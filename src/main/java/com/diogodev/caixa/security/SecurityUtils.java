package com.diogodev.caixa.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new org.springframework.security.authentication.InsufficientAuthenticationException("Sem autenticação");
        }

        Object principal = auth.getPrincipal();

        // teu filtro seta principal = Long userId
        if (principal instanceof Long id) return id;

        // fallback: se em algum canto virar String
        if (principal instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new org.springframework.security.authentication.InsufficientAuthenticationException("Token inválido");
            }
        }

        throw new org.springframework.security.authentication.InsufficientAuthenticationException("Token inválido");
    }
}
