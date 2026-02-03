package com.diogodev.caixa.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        Object p = auth.getPrincipal();
        if (p instanceof Long id) return id;
        if (p instanceof String s) return Long.valueOf(s);
        throw new IllegalStateException("Principal inválido: " + p.getClass().getName());
    }
}
