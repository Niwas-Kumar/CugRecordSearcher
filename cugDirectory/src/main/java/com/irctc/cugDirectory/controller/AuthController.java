package com.irctc.cugDirectory.controller;

import com.irctc.cugDirectory.repository.AppUserRepository;
import com.irctc.cugDirectory.service.AccessLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles all authentication REST endpoints:
 *
 *  POST /api/auth/login           – validate credentials, create session
 *  POST /api/auth/logout          – invalidate session
 *  GET  /api/auth/me              – return current user info (or 401)
 *  POST /api/auth/change-password – change logged-in user's own password
 *
 * No Spring Security form-login filter, no CSRF tokens — just plain HTTP.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessLogService accessLogService;

    public AuthController(AuthenticationManager authenticationManager,
                          AppUserRepository appUserRepository,
                          PasswordEncoder passwordEncoder,
                          AccessLogService accessLogService) {
        this.authenticationManager = authenticationManager;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessLogService = accessLogService;
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Accepts JSON { "username": "...", "password": "..." }.
     * Authenticates against the database, creates a server-side session,
     * and returns { "username": "...", "role": "..." }.
     * Returns 401 on bad credentials or disabled account.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                   HttpServletRequest request) {
        String username = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "");

        if (username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Username and password are required."));
        }

        try {
            // Authenticate — throws on bad credentials or disabled account
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Store the authentication in a new session
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context
            );

            accessLogService.log(username, "login", "");

            String role = auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");

            return ResponseEntity.ok(Map.of("username", username, "role", role));

        } catch (DisabledException e) {
            accessLogService.log(username, "login_failed", "account disabled");
            return ResponseEntity.status(401)
                    .body(Map.of("error", "This account has been disabled. Contact your administrator."));

        } catch (BadCredentialsException e) {
            accessLogService.log(username, "login_failed", "bad credentials");
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid username or password."));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "An unexpected error occurred. Please try again."));
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, Authentication authentication) {
        if (authentication != null) {
            accessLogService.log(authentication.getName(), "logout", "");
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("status", "logged_out"));
    }

    // ── Who am I ──────────────────────────────────────────────────────────────

    /**
     * Returns the current user's username and role.
     * Returns 401 JSON if not authenticated — auth-helper.js requireLogin()
     * uses this 401 to redirect to /login.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated."));
        }
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "role", role
        ));
    }

    // ── Change password ───────────────────────────────────────────────────────

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body,
                                            Authentication authentication) {
        String currentPassword = body.getOrDefault("currentPassword", "");
        String newPassword     = body.getOrDefault("newPassword", "");

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "New password must be at least 6 characters."));
        }

        var userOpt = appUserRepository.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "User account not found."));
        }

        var user = userOpt.get();
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Current password is incorrect."));
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);
        accessLogService.log(authentication.getName(), "password_change", "");

        return ResponseEntity.ok(Map.of("status", "Password updated successfully."));
    }
}
