package com.possaas.possaasbackend.platformadmin;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.platformadmin.dto.PlatformAdminLoginRequest;
import com.possaas.possaasbackend.platformadmin.dto.PlatformAdminLoginResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform/auth")
public class PlatformAdminAuthController {

    private final PlatformAdminAuthenticationService authenticationService;

    public PlatformAdminAuthController(
            PlatformAdminAuthenticationService authenticationService
    ) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<PlatformAdminLoginResponse>> login(
            @Valid @RequestBody PlatformAdminLoginRequest request
    ) {

        PlatformAdminLoginResponse response =
                authenticationService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Platform administrator login successful",
                        response
                )
        );
    }
}