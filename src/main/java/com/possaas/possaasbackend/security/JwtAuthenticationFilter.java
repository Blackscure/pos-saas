
package com.possaas.possaasbackend.security;

import com.possaas.possaasbackend.permission.Permission;
import com.possaas.possaasbackend.platformadmin.PlatformAdmin;
import com.possaas.possaasbackend.platformadmin.PlatformAdminRepository;
import com.possaas.possaasbackend.role.Role;
import com.possaas.possaasbackend.user.User;
import com.possaas.possaasbackend.user.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PlatformAdminRepository platformAdminRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository,
            PlatformAdminRepository platformAdminRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.platformAdminRepository = platformAdminRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            if (!jwtService.isTokenValid(token)) {
                rejectAuthentication(response);
                return;
            }

            String accountType = jwtService.extractAccountType(token);

            // Support older business-user tokens without accountType.
            if (accountType == null) {
                accountType = JwtService.BUSINESS_USER;
            }

            if (JwtService.PLATFORM_ADMIN.equals(accountType)) {
                authenticatePlatformAdmin(token);
            } else if (JwtService.BUSINESS_USER.equals(accountType)) {
                authenticateBusinessUser(token);
            } else {
                rejectAuthentication(response);
                return;
            }

        } catch (Exception exception) {
            rejectAuthentication(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void authenticatePlatformAdmin(String token) {

        var adminId = jwtService.extractPlatformAdminId(token);

        PlatformAdmin admin = platformAdminRepository
                .findById(adminId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Platform administrator not found"
                        )
                );

        if (!admin.isActive() ||
                !"SUPER_ADMIN".equals(admin.getRole())) {
            throw new IllegalStateException(
                    "Platform administrator account is inactive"
            );
        }

        PlatformAdminPrincipal principal =
                new PlatformAdminPrincipal(
                        admin.getId(),
                        admin.getEmail(),
                        admin.getRole()
                );

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Set.of(
                                new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private void authenticateBusinessUser(String token) {

        var userId = jwtService.extractUserId(token);
        var businessId = jwtService.extractBusinessId(token);

        User user = userRepository
                .findByIdWithRolesAndPermissions(userId, businessId)
                .orElseThrow(() ->
                        new IllegalStateException("User account not found")
                );

        if (!user.isActive() ||
                !user.getBusiness().isActive() ||
                user.getBusiness().getTenant() == null ||
                !user.getBusiness().getTenant().isActive()) {

            throw new IllegalStateException(
                    "Business user account is inactive"
            );
        }

        var tenantId = user.getBusiness().getTenant().getId();

        Set<String> roles = user.getRoles()
                .stream()
                .filter(Role::isActive)
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles()
                .stream()
                .filter(Role::isActive)
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        AuthenticatedUser principal = new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                businessId,
                tenantId,
                roles,
                permissions
        );

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        roles.forEach(role ->
                authorities.add(
                        new SimpleGrantedAuthority("ROLE_" + role)
                )
        );

        permissions.forEach(permission ->
                authorities.add(
                        new SimpleGrantedAuthority(permission)
                )
        );

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private void rejectAuthentication(HttpServletResponse response)
            throws IOException {

        SecurityContextHolder.clearContext();
        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or inactive authentication"
        );
    }
}
