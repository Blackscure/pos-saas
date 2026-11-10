package com.possaas.possaasbackend.security;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.security.dto.LoginRequest;
import com.possaas.possaasbackend.security.dto.LoginResponse;
import com.possaas.possaasbackend.user.User;
import com.possaas.possaasbackend.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long accessTokenExpiration;

    public AuthenticationService(
            UserRepository userRepository,
            BusinessRepository businessRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${jwt.access-token-expiration}")
            long accessTokenExpiration
    ) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.accessTokenExpiration = accessTokenExpiration;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        // 1. Find the business
        Business business = businessRepository
                .findById(request.businessId())
                .orElseThrow(this::invalidCredentials);

        // 2. Check whether the business is active
        if (!business.isActive()) {
            throw invalidCredentials();
        }

        // 3. Check whether the tenant is active
        if (business.getTenant() == null ||
                !business.getTenant().isActive()) {
            throw invalidCredentials();
        }

        // 4. Find the user within the specified business
        User user = userRepository
                .findByBusiness_IdAndEmail(
                        request.businessId(),
                        request.email()
                )
                .orElseThrow(this::invalidCredentials);

        // 5. Check whether the user is active
        if (!user.isActive()) {
            throw invalidCredentials();
        }

        // 6. Verify the password
        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw invalidCredentials();
        }

        // 7. Generate the JWT access token
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                business.getId()
        );

        // 8. Return authenticated user information
        return new LoginResponse(
                accessToken,
                "Bearer",
                accessTokenExpiration,
                user.getId(),
                business.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid business ID, email or password, or account is inactive"
        );
    }
}