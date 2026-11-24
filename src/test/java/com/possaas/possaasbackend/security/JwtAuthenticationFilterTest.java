package com.possaas.possaasbackend.security;

import com.possaas.possaasbackend.platformadmin.PlatformAdmin;
import com.possaas.possaasbackend.platformadmin.PlatformAdminRepository;
import com.possaas.possaasbackend.user.UserRepository;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UserRepository userRepository;
    private PlatformAdminRepository platformAdminRepository;

    private JwtAuthenticationFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
        platformAdminRepository = mock(PlatformAdminRepository.class);

        filter = new JwtAuthenticationFilter(
                jwtService,
                userRepository,
                platformAdminRepository
        );

        filterChain = mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(
                jwtService,
                userRepository,
                platformAdminRepository
        );
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsNotBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic sometoken");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(
                jwtService,
                userRepository,
                platformAdminRepository
        );
    }

    @Test
    void shouldRejectInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid("invalid-token"))
                .thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService).isTokenValid("invalid-token");

        verifyNoInteractions(
                userRepository,
                platformAdminRepository,
                filterChain
        );
    }

    @Test
    void shouldRejectExpiredOrMalformedToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer malformed-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid("malformed-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(
                userRepository,
                platformAdminRepository,
                filterChain
        );
    }

    @Test
    void shouldAuthenticateActivePlatformAdmin() throws Exception {

        UUID adminId = UUID.randomUUID();

        PlatformAdmin admin = new PlatformAdmin();
        admin.setId(adminId);
        admin.setEmail("admin@example.com");
        admin.setRole("SUPER_ADMIN");
        admin.setActive(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-admin-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid("valid-admin-token"))
                .thenReturn(true);

        when(jwtService.extractAccountType("valid-admin-token"))
                .thenReturn(JwtService.PLATFORM_ADMIN);

        when(jwtService.extractPlatformAdminId("valid-admin-token"))
                .thenReturn(adminId);

        when(platformAdminRepository.findById(adminId))
                .thenReturn(Optional.of(admin));

        filter.doFilter(request, response, filterChain);

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());

        assertInstanceOf(
                PlatformAdminPrincipal.class,
                authentication.getPrincipal()
        );

        PlatformAdminPrincipal principal =
                (PlatformAdminPrincipal) authentication.getPrincipal();

        assertEquals(adminId, principal.adminId());
        assertEquals("admin@example.com", principal.email());
        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_SUPER_ADMIN")
                        )
        );

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectInactivePlatformAdmin() throws Exception {

        UUID adminId = UUID.randomUUID();

        PlatformAdmin admin = new PlatformAdmin();
        admin.setId(adminId);
        admin.setEmail("admin@example.com");
        admin.setRole("SUPER_ADMIN");
        admin.setActive(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer inactive-admin-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid("inactive-admin-token"))
                .thenReturn(true);

        when(jwtService.extractAccountType("inactive-admin-token"))
                .thenReturn(JwtService.PLATFORM_ADMIN);

        when(jwtService.extractPlatformAdminId("inactive-admin-token"))
                .thenReturn(adminId);

        when(platformAdminRepository.findById(adminId))
                .thenReturn(Optional.of(admin));

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(userRepository, filterChain);
    }

    @Test
    void shouldRejectUnknownAccountType() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer unknown-account-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid("unknown-account-token"))
                .thenReturn(true);

        when(jwtService.extractAccountType("unknown-account-token"))
                .thenReturn("UNKNOWN");

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(
                userRepository,
                platformAdminRepository,
                filterChain
        );
    }

    @Test
    void shouldClearSecurityContext() {

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication
                        .UsernamePasswordAuthenticationToken(
                        "test-user",
                        null
                )
        );

        SecurityContextHolder.clearContext();

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }
}
