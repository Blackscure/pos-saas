
package com.possaas.possaasbackend.user;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.security.AuthenticatedUser;
import com.possaas.possaasbackend.user.dto.CreateUserRequest;
import com.possaas.possaasbackend.user.dto.UpdateUserRequest;
import com.possaas.possaasbackend.user.dto.UserResponse;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create user
    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateUserRequest request
    ) {
        User user = userService.createUser(
                authenticatedUser.tenantId(),
                businessId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "User created successfully",
                        UserResponse.fromEntity(user)
                ));
    }

    // Get all users with pagination
    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        Pageable pageable = PaginationUtils.createPageable(
                page, size, sortBy, sortDir
        );

        Page<User> userPage = userService.getUsersByBusiness(
                authenticatedUser.tenantId(),
                businessId,
                pageable
        );

        List<UserResponse> users = userPage.getContent()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();

        PageResponse<UserResponse> pageResponse =
                PageResponse.of(userPage, users);

        return ResponseEntity.ok(
                ApiResponse.success("Users fetched successfully", pageResponse)
        );
    }

    // Get user by ID
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_VIEW') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID businessId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        User user = userService.getUserById(
                authenticatedUser.tenantId(),
                businessId,
                userId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User fetched successfully",
                        UserResponse.fromEntity(user)
                )
        );
    }

    // Update user
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_UPDATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID businessId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        User user = userService.updateUser(
                authenticatedUser.tenantId(),
                businessId,
                userId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User updated successfully",
                        UserResponse.fromEntity(user)
                )
        );
    }

    // Activate or deactivate user
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('USER_DEACTIVATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID businessId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam boolean active
    ) {
        User user = userService.updateUserStatus(
                authenticatedUser.tenantId(),
                businessId,
                userId,
                active
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        active ? "User activated successfully" : "User deactivated successfully",
                        UserResponse.fromEntity(user)
                )
        );
    }

    // Soft-delete user
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_DEACTIVATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID businessId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        userService.deleteUser(
                authenticatedUser.tenantId(),
                businessId,
                userId
        );

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully", null)
        );
    }
}
