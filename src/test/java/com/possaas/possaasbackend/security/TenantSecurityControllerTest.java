package com.possaas.possaasbackend.security;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessController;
import com.possaas.possaasbackend.business.BusinessService;
import com.possaas.possaasbackend.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(TenantSecurityControllerTest.TestConfig.class)
class TenantSecurityControllerTest {

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {
        @Bean
        public BusinessService businessService() {
            return mock(BusinessService.class);
        }

        @Bean
        public BusinessController businessController(BusinessService businessService) {
            return new BusinessController(businessService);
        }

        @Bean
        public GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }
    }

    @Autowired
    private BusinessController businessController;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    private MockMvc mockMvc;
    private UUID tenantAId;
    private UUID tenantBId;
    private UUID businessAId;
    private UUID businessBId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(businessController)
                .setControllerAdvice(globalExceptionHandler)
                .build();

        tenantAId = UUID.randomUUID();
        tenantBId = UUID.randomUUID();
        businessAId = UUID.randomUUID();
        businessBId = UUID.randomUUID();

        // User authenticated under Tenant A with OWNER role and full business permissions
        AuthenticatedUser userFromTenantA = new AuthenticatedUser(
                UUID.randomUUID(),
                "owner@tenanta.com",
                businessAId,
                tenantAId,
                Set.of("OWNER"),
                Set.of("BUSINESS_VIEW", "BUSINESS_CREATE", "BUSINESS_UPDATE", "BUSINESS_DELETE")
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userFromTenantA,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_OWNER"),
                        new SimpleGrantedAuthority("BUSINESS_VIEW"),
                        new SimpleGrantedAuthority("BUSINESS_CREATE"),
                        new SimpleGrantedAuthority("BUSINESS_UPDATE"),
                        new SimpleGrantedAuthority("BUSINESS_DELETE")
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("User from Tenant A receives 403 Forbidden when fetching Tenant B's businesses")
    void shouldDenyAccessWhenFetchingTenantBBusinesses() throws Exception {
        mockMvc.perform(get("/api/tenants/" + tenantBId + "/businesses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User from Tenant A receives 403 Forbidden when fetching a specific Business under Tenant B")
    void shouldDenyAccessWhenFetchingTenantBBusinessById() throws Exception {
        mockMvc.perform(get("/api/tenants/" + tenantBId + "/businesses/" + businessBId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User from Tenant A receives 403 Forbidden when creating a Business under Tenant B")
    void shouldDenyAccessWhenCreatingBusinessUnderTenantB() throws Exception {
        String payload = """
                {
                    "name": "Malicious Business",
                    "businessType": "RETAIL"
                }
                """;

        mockMvc.perform(post("/api/tenants/" + tenantBId + "/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User from Tenant A receives 403 Forbidden when deleting a Business under Tenant B")
    void shouldDenyAccessWhenDeletingBusinessUnderTenantB() throws Exception {
        mockMvc.perform(delete("/api/tenants/" + tenantBId + "/businesses/" + businessBId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User from Tenant A is allowed to fetch their own Tenant A businesses")
    void shouldAllowAccessWhenFetchingOwnTenantABusinesses() throws Exception {
        when(businessService.getBusinessesByTenant(eq(tenantAId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Business())));

        mockMvc.perform(get("/api/tenants/" + tenantAId + "/businesses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
