package com.possaas.possaasbackend.platformadmin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformAdminInitializerTest {

    @Mock
    private PlatformAdminRepository platformAdminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PlatformAdminInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new PlatformAdminInitializer(
                platformAdminRepository,
                passwordEncoder
        );
    }

    @Test
    void shouldSkipWhenBootstrapDisabled() {
        ReflectionTestUtils.setField(initializer, "bootstrapEnabled", false);

        initializer.run(new DefaultApplicationArguments());

        verifyNoInteractions(platformAdminRepository, passwordEncoder);
    }

    @Test
    void shouldCreateAdminWhenBootstrapEnabledAndAdminDoesNotExist() {
        ReflectionTestUtils.setField(initializer, "bootstrapEnabled", true);
        ReflectionTestUtils.setField(initializer, "firstName", "Kesa");
        ReflectionTestUtils.setField(initializer, "lastName", "Yahi");
        ReflectionTestUtils.setField(initializer, "email", "kesayahi@gmail.com");
        ReflectionTestUtils.setField(initializer, "password", "StrongPassword123!");

        when(platformAdminRepository.existsByEmailIgnoreCase("kesayahi@gmail.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("StrongPassword123!"))
                .thenReturn("encodedPasswordHash");

        initializer.run(new DefaultApplicationArguments());

        ArgumentCaptor<PlatformAdmin> captor = ArgumentCaptor.forClass(PlatformAdmin.class);
        verify(platformAdminRepository).save(captor.capture());

        PlatformAdmin savedAdmin = captor.getValue();
        assertEquals("Kesa", savedAdmin.getFirstName());
        assertEquals("Yahi", savedAdmin.getLastName());
        assertEquals("kesayahi@gmail.com", savedAdmin.getEmail());
        assertEquals("encodedPasswordHash", savedAdmin.getPasswordHash());
        assertEquals("SUPER_ADMIN", savedAdmin.getRole());
        assertTrue(savedAdmin.isActive());
    }

    @Test
    void shouldSkipWhenAdminAlreadyExists() {
        ReflectionTestUtils.setField(initializer, "bootstrapEnabled", true);
        ReflectionTestUtils.setField(initializer, "firstName", "Kesa");
        ReflectionTestUtils.setField(initializer, "lastName", "Yahi");
        ReflectionTestUtils.setField(initializer, "email", "kesayahi@gmail.com");
        ReflectionTestUtils.setField(initializer, "password", "StrongPassword123!");

        when(platformAdminRepository.existsByEmailIgnoreCase("kesayahi@gmail.com"))
                .thenReturn(true);

        initializer.run(new DefaultApplicationArguments());

        verify(platformAdminRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowWhenPasswordTooShort() {
        ReflectionTestUtils.setField(initializer, "bootstrapEnabled", true);
        ReflectionTestUtils.setField(initializer, "firstName", "Kesa");
        ReflectionTestUtils.setField(initializer, "lastName", "Yahi");
        ReflectionTestUtils.setField(initializer, "email", "kesayahi@gmail.com");
        ReflectionTestUtils.setField(initializer, "password", "short");

        assertThrows(
                IllegalStateException.class,
                () -> initializer.run(new DefaultApplicationArguments())
        );
    }
}
