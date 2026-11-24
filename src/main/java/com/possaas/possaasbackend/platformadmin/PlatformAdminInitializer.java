package com.possaas.possaasbackend.platformadmin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PlatformAdminInitializer implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(PlatformAdminInitializer.class);

    private final PlatformAdminRepository platformAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${PLATFORM_ADMIN_BOOTSTRAP_ENABLED:false}")
    private boolean bootstrapEnabled;

    @Value("${PLATFORM_ADMIN_FIRST_NAME:}")
    private String firstName;

    @Value("${PLATFORM_ADMIN_LAST_NAME:}")
    private String lastName;

    @Value("${PLATFORM_ADMIN_EMAIL:}")
    private String email;

    @Value("${PLATFORM_ADMIN_PASSWORD:}")
    private String password;

    public PlatformAdminInitializer(
            PlatformAdminRepository platformAdminRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.platformAdminRepository = platformAdminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (!bootstrapEnabled) {
            log.info("Platform admin bootstrap is disabled.");
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (firstName.isBlank() ||
                lastName.isBlank() ||
                normalizedEmail.isBlank() ||
                password.isBlank()) {
            throw new IllegalStateException(
                    "Platform admin bootstrap requires first name, " +
                            "last name, email and password."
            );
        }

        if (password.length() < 12) {
            throw new IllegalStateException(
                    "Platform admin password must contain at least 12 characters."
            );
        }

        if (platformAdminRepository.existsByEmailIgnoreCase(
                normalizedEmail
        )) {
            log.info(
                    "Platform admin already exists. Bootstrap skipped."
            );
            return;
        }

        PlatformAdmin admin = new PlatformAdmin();
        admin.setFirstName(firstName.trim());
        admin.setLastName(lastName.trim());
        admin.setEmail(normalizedEmail);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole("SUPER_ADMIN");
        admin.setActive(true);

        platformAdminRepository.save(admin);

        log.info("Initial platform administrator created successfully.");
    }
}
