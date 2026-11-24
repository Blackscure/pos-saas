package com.possaas.possaasbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String BUSINESS_USER = "BUSINESS_USER";
    public static final String PLATFORM_ADMIN = "PLATFORM_ADMIN";

    private final SecretKey secretKey;
    private final long accessTokenExpiration;

    public JwtService(
            SecretKey secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration
    ) {
        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
    }

    /**
     * Generate an access token for a business user.
     */
    public String generateAccessToken(
            UUID userId,
            String email,
            UUID businessId
    ) {
        Date issuedAt = new Date();
        Date expiration = new Date(
                issuedAt.getTime() + accessTokenExpiration
        );

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("businessId", businessId.toString())
                .claim("accountType", BUSINESS_USER)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate an access token for a platform administrator.
     */
    public String generatePlatformAdminAccessToken(
            UUID adminId,
            String email
    ) {
        Date issuedAt = new Date();
        Date expiration = new Date(
                issuedAt.getTime() + accessTokenExpiration
        );

        return Jwts.builder()
                .subject(adminId.toString())
                .claim("email", email)
                .claim("accountType", PLATFORM_ADMIN)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public UUID extractUserId(String token) {
        String subject = extractClaims(token).getSubject();
        return UUID.fromString(subject);
    }

    public UUID extractPlatformAdminId(String token) {
        String subject = extractClaims(token).getSubject();
        return UUID.fromString(subject);
    }

    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    public UUID extractBusinessId(String token) {
        String businessId = extractClaims(token)
                .get("businessId", String.class);

        return UUID.fromString(businessId);
    }

    public String extractAccountType(String token) {
        return extractClaims(token).get("accountType", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
