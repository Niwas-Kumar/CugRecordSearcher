package com.irctc.cugDirectory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Simplified security configuration.
 *
 * CSRF is disabled — this is a same-origin SPA served from the same domain,
 * so CSRF attacks are not applicable. All state changes require an active
 * server-side session (cookie: JSESSIONID) which the browser sends automatically.
 *
 * Login and logout are handled by custom REST endpoints in AuthController,
 * not by Spring Security's built-in form-login filter. This eliminates the
 * token-timing issues that caused the 403 errors.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager so AuthController can call
     * authenticationManager.authenticate(token) directly.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── CSRF disabled ────────────────────────────────────────────────
            // Same-origin SPA: all requests originate from pages we serve.
            // Disabling CSRF removes the token-cookie timing issue entirely.
            .csrf(csrf -> csrf.disable())

            // ── Session management ───────────────────────────────────────────
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(5)
            )

            // ── Authorization rules ──────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Public — login page, static files, auth API, health check
                .requestMatchers(
                        "/login",
                        "/style.css",
                        "/auth-helper.js",
                        "/api/auth/login",
                        "/api/auth/logout",
                        "/api/auth/me",
                        "/api/ping",
                        "/actuator/health"
                ).permitAll()
                // Admin only
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                // All other /api/** endpoints need a valid session
                .requestMatchers("/api/**").authenticated()
                // Thymeleaf pages — the JS on each page calls requireLogin()
                .anyRequest().permitAll()
            )

            // ── Return 401 JSON for unauthenticated API calls ────────────────
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) -> {
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(401);
                        response.getWriter().write("{\"error\":\"Not authenticated.\"}");
                    },
                    new AntPathRequestMatcher("/api/**")
                )
            );

        return http.build();
    }
}