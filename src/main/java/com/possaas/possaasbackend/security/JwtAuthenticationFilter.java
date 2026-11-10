
package com.possaas.possaasbackend.security;

import com.possaas.possaasbackend.permission.Permission;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
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
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            UUID userId = jwtService.extractUserId(token);
            UUID businessId = jwtService.extractBusinessId(token);

            User user = userRepository
                    .findByIdWithRolesAndPermissions(userId, businessId)
                    .orElseThrow(() ->
                            new IllegalStateException("User account not found")
                    );

            // Verify account, business and tenant status.
            if (!user.isActive() ||
                    !user.getBusiness().isActive() ||
                    user.getBusiness().getTenant() == null ||
                    !user.getBusiness().getTenant().isActive()) {

                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Extract tenant ID from the user's business.
            UUID tenantId = user.getBusiness().getTenant().getId();

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

            // Create authenticated principal.
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    user.getId(),
                    user.getEmail(),
                    businessId,
                    tenantId,
                    roles,
                    permissions
            );

            Set<SimpleGrantedAuthority> authorities = new HashSet<>();

            // Add role authorities.
            roles.forEach(role ->
                    authorities.add(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    )
            );

            // Add permission authorities.
            permissions.forEach(permission ->
                    authorities.add(
                            new SimpleGrantedAuthority(permission)
                    )
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authenticatedUser,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
