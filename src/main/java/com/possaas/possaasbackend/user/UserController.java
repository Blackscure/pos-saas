package com.possaas.possaasbackend.user;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.user.dto.CreateUserRequest;
import com.possaas.possaasbackend.user.dto.UpdateUserRequest;
import com.possaas.possaasbackend.user.dto.UserResponse;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @PathVariable UUID businessId,
            @Valid @RequestBody CreateUserRequest request
    ) {
        User user = userService.createUser(businessId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "User created successfully",
                        UserResponse.fromEntity(user)
                ));
    }

    // Get all users with pagination
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @PathVariable UUID businessId,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        Pageable pageable = PaginationUtils.createPageable(
                page, size, sortBy, sortDir
        );

        Page<User> userPage = userService.getUsersByBusiness(
                businessId, pageable
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
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID businessId,
            @PathVariable UUID userId
    ) {
        User user = userService.getUserById(businessId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User fetched successfully",
                        UserResponse.fromEntity(user)
                )
        );
    }

    // Update user
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID businessId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        User user = userService.updateUser(businessId, userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User updated successfully",
                        UserResponse.fromEntity(user)
                )
        );
    }

    // Activate or deactivate user
    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID businessId,
            @PathVariable UUID userId,
            @RequestParam boolean active
    ) {
        User user = userService.updateUserStatus(
                businessId, userId, active
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
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID businessId,
            @PathVariable UUID userId
    ) {
        userService.deleteUser(businessId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully", null)
        );
    }
}