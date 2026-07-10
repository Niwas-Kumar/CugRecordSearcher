package com.irctc.cugDirectory.loader;

import com.irctc.cugDirectory.model.AppUser;
import com.irctc.cugDirectory.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs on every startup, alongside ExcelDataLoader. Creates the very first
 * admin account if none exists yet, using ADMIN_USERNAME / ADMIN_PASSWORD
 * environment variables (falls back to admin / changeme123 if not set —
 * change this immediately after first login).
 */
@Component
public class AdminAccountSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountSeeder.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default-username:admin}")
    private String defaultAdminUsername;

    @Value("${admin.default-password:changeme123}")
    private String defaultAdminPassword;

    public AdminAccountSeeder(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (appUserRepository.existsByRole("ADMIN")) {
            log.info("An admin account already exists — skipping admin creation.");
            return;
        }

        AppUser admin = new AppUser(
                defaultAdminUsername,
                passwordEncoder.encode(defaultAdminPassword),
                "ADMIN"
        );
        appUserRepository.save(admin);
        log.warn("==============================================================");
        log.warn("Created initial admin account -> username: '{}'", defaultAdminUsername);
        log.warn("Please log in and change this password immediately.");
        log.warn("==============================================================");
    }
}