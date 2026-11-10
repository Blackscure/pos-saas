
package com.possaas.possaasbackend.user;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.user.dto.CreateUserRequest;
import com.possaas.possaasbackend.user.dto.UpdateUserRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UUID tenantId;
    private UUID businessId;
    private UUID userId;

    private Business business;
    private User user;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        userId = UUID.randomUUID();

        business = mock(Business.class);

        user = new User();
        user.setBusiness(business);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setPhone("+254712345678");
        user.setPassword("hashed-password");
        user.setActive(true);
    }

    // 1. Successfully create a user
    @Test
    void shouldCreateUserSuccessfully() {

        when(business.isActive()).thenReturn(true);

        CreateUserRequest request = new CreateUserRequest(
                "John",
                "Doe",
                "john@example.com",
                "+254712345678",
                "password123"
        );

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        when(userRepository.existsByBusiness_IdAndEmail(
                businessId, "john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(tenantId, businessId, request);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("hashed-password", result.getPassword());
        assertTrue(result.isActive());

        verify(businessRepository)
                .findByIdAndTenantId(businessId, tenantId);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    // 2. Reject creation when business does not exist in tenant
    @Test
    void shouldRejectUserCreationWhenBusinessDoesNotExist() {

        CreateUserRequest request = new CreateUserRequest(
                "John", "Doe", "john@example.com",
                null, "password123"
        );

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.createUser(tenantId, businessId, request));

        verify(userRepository, never()).save(any(User.class));
    }

    // 3. Reject creation under an inactive business
    @Test
    void shouldRejectUserCreationWhenBusinessIsInactive() {

        when(business.isActive()).thenReturn(false);

        CreateUserRequest request = new CreateUserRequest(
                "John", "Doe", "john@example.com",
                null, "password123"
        );

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        assertThrows(RuntimeException.class,
                () -> userService.createUser(tenantId, businessId, request));

        verify(userRepository, never()).save(any(User.class));
    }

    // 4. Reject duplicate email addresses
    @Test
    void shouldRejectDuplicateEmail() {

        when(business.isActive()).thenReturn(true);

        CreateUserRequest request = new CreateUserRequest(
                "John", "Doe", "john@example.com",
                null, "password123"
        );

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        when(userRepository.existsByBusiness_IdAndEmail(
                businessId, "john@example.com"))
                .thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> userService.createUser(tenantId, businessId, request));

        verify(userRepository, never()).save(any(User.class));
    }

    // 5. Normalize email to lowercase
    @Test
    void shouldNormalizeEmailWhenCreatingUser() {

        when(business.isActive()).thenReturn(true);

        CreateUserRequest request = new CreateUserRequest(
                "John", "Doe", " JOHN@EXAMPLE.COM ",
                null, "password123"
        );

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        when(userRepository.existsByBusiness_IdAndEmail(
                businessId, "john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(tenantId, businessId, request);

        assertEquals("john@example.com", result.getEmail());
    }

    // 6. Retrieve users belonging to a business
    @Test
    void shouldGetUsersByBusiness() {

        PageRequest pageable = PageRequest.of(0, 10);
        Page<User> expectedPage = new PageImpl<>(List.of(user));

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        when(userRepository.findAllByBusiness_Id(businessId, pageable))
                .thenReturn(expectedPage);

        Page<User> result = userService.getUsersByBusiness(
                tenantId, businessId, pageable
        );

        assertEquals(1, result.getTotalElements());

        verify(businessRepository)
                .findByIdAndTenantId(businessId, tenantId);
        verify(userRepository).findAllByBusiness_Id(businessId, pageable);
    }

    // 7. Reject retrieval for a business outside the tenant
    @Test
    void shouldRejectGettingUsersWhenBusinessDoesNotExistInTenant() {

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.getUsersByBusiness(
                        tenantId, businessId, PageRequest.of(0, 10)
                ));

        verify(userRepository, never())
                .findAllByBusiness_Id(any(), any());
    }

    // 8. Retrieve a user by ID
    @Test
    void shouldGetUserById() {

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        when(userRepository.findByIdAndBusiness_Id(userId, businessId))
                .thenReturn(Optional.of(user));

        User result = userService.getUserById(
                tenantId, businessId, userId
        );

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
    }

    // 9. Reject a user outside the specified business
    @Test
    void shouldRejectGettingUserOutsideBusiness() {

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        when(userRepository.findByIdAndBusiness_Id(userId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.getUserById(
                        tenantId, businessId, userId
                ));
    }

    // 10. Successfully update user information
    @Test
    void shouldUpdateUserSuccessfully() {

        UpdateUserRequest request = new UpdateUserRequest(
                "Jane",
                "Doe",
                "jane@example.com",
                "+254700000000"
        );

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        when(userRepository.findByIdAndBusiness_Id(userId, businessId))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByBusiness_IdAndEmail(
                businessId, "jane@example.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(
                tenantId, businessId, userId, request
        );

        assertEquals("Jane", result.getFirstName());
        assertEquals("jane@example.com", result.getEmail());

        // Updating profile details must not change the password
        assertEquals("hashed-password", result.getPassword());
    }

    // 11. Reject duplicate email during update
    @Test
    void shouldRejectDuplicateEmailDuringUpdate() {

        UpdateUserRequest request = new UpdateUserRequest(
                "Jane",
                "Doe",
                "another@example.com",
                null
        );

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        when(userRepository.findByIdAndBusiness_Id(userId, businessId))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByBusiness_IdAndEmail(
                businessId, "another@example.com"))
                .thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> userService.updateUser(
                        tenantId, businessId, userId, request
                ));

        verify(userRepository, never()).save(any(User.class));
    }

    // 12. Activate or deactivate a user
    @Test
    void shouldDeactivateUserSuccessfully() {

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        when(userRepository.findByIdAndBusiness_Id(userId, businessId))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUserStatus(
                tenantId, businessId, userId, false
        );

        assertFalse(result.isActive());
    }

    // 13. Soft-delete a user
    @Test
    void shouldSoftDeleteUserSuccessfully() {

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        when(userRepository.findByIdAndBusiness_Id(userId, businessId))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.deleteUser(tenantId, businessId, userId);

        assertFalse(user.isActive());

        verify(userRepository).save(user);
    }

    // 14. Reject access when business belongs to another tenant
    @Test
    void shouldRejectAccessWhenBusinessBelongsToAnotherTenant() {

        UUID anotherTenantId = UUID.randomUUID();

        when(businessRepository.findByIdAndTenantId(
                businessId, anotherTenantId
        )).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.getUsersByBusiness(
                        anotherTenantId,
                        businessId,
                        PageRequest.of(0, 10)
                ));

        verify(userRepository, never())
                .findAllByBusiness_Id(any(), any());
    }
}
