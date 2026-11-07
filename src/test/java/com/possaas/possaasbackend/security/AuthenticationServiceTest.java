
package com.possaas.possaasbackend.security;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.security.dto.LoginRequest;
import com.possaas.possaasbackend.security.dto.LoginResponse;
import com.possaas.possaasbackend.tenant.Tenant;
import com.possaas.possaasbackend.user.User;
import com.possaas.possaasbackend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private Business business;

    @Mock
    private Tenant tenant;

    @Mock
    private User user;

    private AuthenticationService authenticationService;

    private UUID businessId;
    private UUID userId;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository,
                businessRepository,
                passwordEncoder,
                jwtService,
                900000L
        );

        businessId = UUID.randomUUID();
        userId = UUID.randomUUID();

        loginRequest = new LoginRequest(
                businessId,
                "john@example.com",
                "John@12345"
        );
    }

    @Test
    void shouldLoginSuccessfully() {
        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));

        when(business.getId()).thenReturn(businessId);
        when(business.isActive()).thenReturn(true);
        when(business.getTenant()).thenReturn(tenant);
        when(tenant.isActive()).thenReturn(true);

        when(userRepository.findByBusiness_IdAndEmail(
                businessId,
                "john@example.com"
        )).thenReturn(Optional.of(user));

        when(user.isActive()).thenReturn(true);
        when(user.getPassword()).thenReturn("encoded-password");
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("john@example.com");
        when(user.getFirstName()).thenReturn("John");
        when(user.getLastName()).thenReturn("Doe");

        when(passwordEncoder.matches(
                "John@12345",
                "encoded-password"
        )).thenReturn(true);

        when(jwtService.generateAccessToken(
                userId,
                "john@example.com",
                businessId
        )).thenReturn("mock-access-token");

        LoginResponse response = authenticationService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mock-access-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900000L, response.expiresIn());
        assertEquals(userId, response.userId());
        assertEquals(businessId, response.businessId());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("john@example.com", response.email());

        verify(passwordEncoder).matches(
                "John@12345",
                "encoded-password"
        );

        verify(jwtService).generateAccessToken(
                userId,
                "john@example.com",
                businessId
        );
    }

    @Test
    void shouldRejectLoginWhenBusinessDoesNotExist() {
        when(businessRepository.findById(businessId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.login(loginRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verifyNoInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldRejectLoginWhenBusinessIsInactive() {
        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));

        when(business.isActive()).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.login(loginRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verifyNoInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldRejectLoginWhenTenantIsInactive() {
        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));

        when(business.isActive()).thenReturn(true);
        when(business.getTenant()).thenReturn(tenant);
        when(tenant.isActive()).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.login(loginRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verifyNoInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldRejectLoginWhenUserIsInactive() {
        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));

        when(business.isActive()).thenReturn(true);
        when(business.getTenant()).thenReturn(tenant);
        when(tenant.isActive()).thenReturn(true);

        when(userRepository.findByBusiness_IdAndEmail(
                businessId,
                "john@example.com"
        )).thenReturn(Optional.of(user));

        when(user.isActive()).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.login(loginRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void shouldRejectLoginWhenPasswordIsIncorrect() {
        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));

        when(business.isActive()).thenReturn(true);
        when(business.getTenant()).thenReturn(tenant);
        when(tenant.isActive()).thenReturn(true);

        when(userRepository.findByBusiness_IdAndEmail(
                businessId,
                "john@example.com"
        )).thenReturn(Optional.of(user));

        when(user.isActive()).thenReturn(true);
        when(user.getPassword()).thenReturn("encoded-password");

        when(passwordEncoder.matches(
                "John@12345",
                "encoded-password"
        )).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.login(loginRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verify(jwtService, never()).generateAccessToken(
                any(), any(), any()
        );
    }
}
