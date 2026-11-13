package com.possaas.possaasbackend.platformadmin;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.user.User;
import com.possaas.possaasbackend.user.UserService;
import com.possaas.possaasbackend.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/users")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformUserController {

    private final UserService userService;

    public PlatformUserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users across all tenants
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        Pageable pageable = PaginationUtils.createPageable(
                page, size, sortBy, sortDir
        );

        Page<User> userPage = userService.getAllUsers(pageable);

        List<UserResponse> responses = userPage.getContent()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();

        PageResponse<UserResponse> pageResponse =
                PageResponse.of(userPage, responses);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Users retrieved successfully",
                        pageResponse
                )
        );
    }

    // Get a user by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID id
    ) {
        User user = userService.getPlatformUserById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User retrieved successfully",
                        UserResponse.fromEntity(user)
                )
        );
    }

    // Activate or suspend a user
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        User user = userService.updatePlatformUserStatus(id, active);

        String message = active
                ? "User activated successfully"
                : "User suspended successfully";

        return ResponseEntity.ok(
                ApiResponse.success(
                        message,
                        UserResponse.fromEntity(user)
                )
        );
    }
}
