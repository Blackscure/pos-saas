package com.possaas.possaasbackend.security;

import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private UUID userId;
    private UUID businessId;

    private final String email = "john@example.com";

    @BeforeEach
    void setUp() {
        SecretKey secretKey = Keys.hmacShaKeyFor(
                "01234567890123456789012345678901"
                        .getBytes(StandardCharsets.UTF_8)
        );

        jwtService = new JwtService(secretKey, 900000);

        userId = UUID.randomUUID();
        businessId = UUID.randomUUID();
    }

    @Test
    void shouldGenerateValidAccessToken() {

        String token = jwtService.generateAccessToken(
                userId,
                email,
                businessId
        );

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldExtractUserIdFromToken() {

        String token = jwtService.generateAccessToken(
                userId,
                email,
                businessId
        );

        UUID extractedUserId = jwtService.extractUserId(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void shouldExtractEmailFromToken() {

        String token = jwtService.generateAccessToken(
                userId,
                email,
                businessId
        );

        String extractedEmail = jwtService.extractEmail(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    void shouldExtractBusinessIdFromToken() {

        String token = jwtService.generateAccessToken(
                userId,
                email,
                businessId
        );

        UUID extractedBusinessId =
                jwtService.extractBusinessId(token);

        assertEquals(businessId, extractedBusinessId);
    }

    @Test
    void shouldRejectInvalidToken() {

        String invalidToken = "invalid.jwt.token";

        assertFalse(jwtService.isTokenValid(invalidToken));
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {

        String token = jwtService.generateAccessToken(
                userId,
                email,
                businessId
        );

        SecretKey differentKey = Keys.hmacShaKeyFor(
                "98765432109876543210987654321098"
                        .getBytes(StandardCharsets.UTF_8)
        );

        JwtService anotherJwtService =
                new JwtService(differentKey, 900000);

        assertFalse(anotherJwtService.isTokenValid(token));
    }
}
