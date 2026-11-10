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
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extract the user ID from a token.
     */
    public UUID extractUserId(String token) {
        String subject = extractClaims(token).getSubject();
        return UUID.fromString(subject);
    }

    /**
     * Extract the email from a token.
     */
    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    /**
     * Extract the business ID from a token.
     */
    public UUID extractBusinessId(String token) {
        String businessId = extractClaims(token)
                .get("businessId", String.class);

        return UUID.fromString(businessId);
    }

    /**
     * Validate the token signature and expiration.
     */
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Parse and verify JWT claims.
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
