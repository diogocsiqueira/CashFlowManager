package com.diogodev.caixa.shared.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {

        String path = req.getServletPath();

        boolean publicAuthRoute =
                path.equals("/api/auth/login") ||
                        path.equals("/api/auth/register") ||
                        path.equals("/api/auth/refresh") ||
                        path.equals("/api/auth/logout") ||
                        path.equals("/api/mobile/auth/login") ||
                        path.equals("/api/mobile/auth/refresh") ||
                        path.equals("/api/mobile/auth/logout");

        if (publicAuthRoute) {
            chain.doFilter(req, res);
            return;
        }

        String auth = req.getHeader("Authorization");

        if (auth == null || !auth.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = auth.substring(7).trim();

        try {
            Claims claims = jwtService.parseClaims(token);

            Object rolesObj = claims.get("roles");

            Set<String> roles = (rolesObj instanceof Collection<?> col)
                    ? col.stream().map(String::valueOf).collect(Collectors.toSet())
                    : Set.of();

            var authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet());

            Long userId = Long.valueOf(claims.getSubject());

            var authToken = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();

            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("""
        {
          "error": "Não autenticado",
          "message": "Token inválido ou expirado"
        }
    """);

            return;
        }

        chain.doFilter(req, res);
    }
}