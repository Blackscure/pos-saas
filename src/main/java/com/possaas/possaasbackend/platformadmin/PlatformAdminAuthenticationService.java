package com.possaas.possaasbackend.platformadmin;

import com.possaas.possaasbackend.platformadmin.dto.PlatformAdminLoginRequest;
import com.possaas.possaasbackend.platformadmin.dto.PlatformAdminLoginResponse;
import com.possaas.possaasbackend.security.JwtService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlatformAdminAuthenticationService {

    private final PlatformAdminRepository platformAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long accessTokenExpiration;

    public PlatformAdminAuthenticationService(
            PlatformAdminRepository platformAdminRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${jwt.access-token-expiration}")
            long accessTokenExpiration
    ) {
        this.platformAdminRepository = platformAdminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.accessTokenExpiration = accessTokenExpiration;
    }

    @Transactional(readOnly = true)
    public PlatformAdminLoginResponse login(
            PlatformAdminLoginRequest request
    ) {

        String normalizedEmail = request.email().trim().toLowerCase();

        PlatformAdmin admin = platformAdminRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(this::invalidCredentials);

        if (!admin.isActive() ||
                !"SUPER_ADMIN".equals(admin.getRole())) {
            throw invalidCredentials();
        }

        if (!passwordEncoder.matches(
                request.password(),
                admin.getPasswordHash()
        )) {
            throw invalidCredentials();
        }

        String accessToken =
                jwtService.generatePlatformAdminAccessToken(
                        admin.getId(),
                        admin.getEmail()
                );

        return new PlatformAdminLoginResponse(
                accessToken,
                "Bearer",
                accessTokenExpiration,
                admin.getId(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getEmail(),
                admin.getRole()
        );
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password, or administrator account is inactive"
        );
    }
}