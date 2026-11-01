package com.possaas.possaasbackend.tenant;

import com.possaas.possaasbackend.common.exception.GlobalExceptionHandler;
import com.possaas.possaasbackend.tenant.dto.TenantRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TenantControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private TenantController tenantController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(tenantController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTenant_Success() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setName("Test Tenant");
        tenant.setCode("TEST01");
        tenant.setEmail("tenant@example.com");
        tenant.setActive(true);

        when(tenantService.createTenant(any(TenantRequest.class))).thenReturn(tenant);

        String json = """
                {
                    "name": "Test Tenant",
                    "code": "TEST01",
                    "email": "tenant@example.com",
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tenant registered successfully"))
                .andExpect(jsonPath("$.data.name").value("Test Tenant"))
                .andExpect(jsonPath("$.data.code").value("TEST01"))
                .andExpect(jsonPath("$.data.email").value("tenant@example.com"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void createTenant_DuplicateCode_ReturnsFalseWithMessage() throws Exception {
        when(tenantService.createTenant(any(TenantRequest.class)))
                .thenThrow(new IllegalArgumentException("A tenant with this business code already exists"));

        String json = """
                {
                    "name": "Test Tenant",
                    "code": "TEST01",
                    "email": "tenant@example.com"
                }
                """;

        mockMvc.perform(post("/api/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("A tenant with this business code already exists"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void updateTenant_Success() throws Exception {
        UUID id = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setName("Updated Tenant");
        tenant.setCode("UPDATE01");
        tenant.setEmail("updated@example.com");
        tenant.setActive(true);

        when(tenantService.updateTenant(eq(id), any(TenantRequest.class))).thenReturn(tenant);

        String json = """
                {
                    "name": "Updated Tenant",
                    "code": "UPDATE01",
                    "email": "updated@example.com",
                    "active": true
                }
                """;

        mockMvc.perform(put("/api/tenants/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tenant updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Updated Tenant"))
                .andExpect(jsonPath("$.data.code").value("UPDATE01"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"));
    }

    @Test
    void updateTenant_NotFound_ReturnsFalseWithMessage() throws Exception {
        UUID id = UUID.randomUUID();
        when(tenantService.updateTenant(eq(id), any(TenantRequest.class)))
                .thenThrow(new IllegalArgumentException("Tenant not found"));

        String json = """
                {
                    "name": "Updated Tenant",
                    "code": "UPDATE01",
                    "email": "updated@example.com"
                }
                """;

        mockMvc.perform(put("/api/tenants/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tenant not found"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
    }
}
