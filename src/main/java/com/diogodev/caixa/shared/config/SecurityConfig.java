package com.diogodev.caixa.shared.config;

import com.diogodev.caixa.shared.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // (depois a gente melhora no refresh/cookie)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // auth sempre público
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/api/mobile/auth/login",
                                "/api/mobile/auth/refresh",
                                "/api/mobile/auth/logout"
                        ).permitAll()

                        .requestMatchers("/api/auth/me").authenticated()

                        // h2 console (dev)
                        .requestMatchers("/h2/**").permitAll()

                        // OPTIONS (preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        //Swagger
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // resto protegido
                        .requestMatchers("/api/**").authenticated()

                        // se tiver algo fora de /api
                        .anyRequest().permitAll()
                )
                .headers(h -> h.frameOptions(f -> f.disable())) // H2 console usa frame
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
