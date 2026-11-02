package com.possaas.possaasbackend.business;

import com.possaas.possaasbackend.business.dto.BusinessRequest;
import com.possaas.possaasbackend.common.exception.GlobalExceptionHandler;
import com.possaas.possaasbackend.tenant.Tenant;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BusinessControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BusinessService businessService;

    @InjectMocks
    private BusinessController businessController;

    private UUID tenantId;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(businessController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        tenantId = UUID.randomUUID();
        tenant = new Tenant();
        tenant.setName("Test Tenant");
        tenant.setCode("TEST01");
        tenant.setEmail("tenant@example.com");
        tenant.setActive(true);
    }

    @Test
    void createBusiness_Success() throws Exception {
        UUID businessId = UUID.randomUUID();
        Business business = new Business();
        business.setId(businessId);
        business.setName("Supermart Downtown");
        business.setBusinessType(BusinessType.SUPERMARKET);
        business.setRegistrationNumber("REG-12345");
        business.setPhone("+254700000000");
        business.setEmail("info@supermart.com");
        business.setAddress("Kenyatta Ave");
        business.setCurrency("KES");
        business.setTaxEnabled(true);
        business.setActive(true);
        business.setTenant(tenant);

        when(businessService.createBusiness(eq(tenantId), any(BusinessRequest.class)))
                .thenReturn(business);

        String json = """
                {
                    "name": "Supermart Downtown",
                    "businessType": "SUPERMARKET",
                    "registrationNumber": "REG-12345",
                    "phone": "+254700000000",
                    "email": "info@supermart.com",
                    "address": "Kenyatta Ave",
                    "currency": "KES",
                    "taxEnabled": true,
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/tenants/" + tenantId + "/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Business created successfully"))
                .andExpect(jsonPath("$.data.id").value(businessId.toString()))
                .andExpect(jsonPath("$.data.name").value("Supermart Downtown"))
                .andExpect(jsonPath("$.data.businessType").value("SUPERMARKET"))
                .andExpect(jsonPath("$.data.registrationNumber").value("REG-12345"))
                .andExpect(jsonPath("$.data.currency").value("KES"))
                .andExpect(jsonPath("$.data.taxEnabled").value(true))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.status").value(true));
    }

    @Test
    void createBusiness_DuplicateName_ReturnsFalseWithMessage() throws Exception {
        when(businessService.createBusiness(eq(tenantId), any(BusinessRequest.class)))
                .thenThrow(new IllegalArgumentException("A business with this name already exists for this tenant"));

        String json = """
                {
                    "name": "Supermart Downtown",
                    "businessType": "SUPERMARKET"
                }
                """;

        mockMvc.perform(post("/api/tenants/" + tenantId + "/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("A business with this name already exists for this tenant"))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));
    }

    @Test
    void createBusiness_ValidationError_ReturnsFalseWithMessage() throws Exception {
        String json = """
                {
                    "name": "",
                    "businessType": null
                }
                """;

        mockMvc.perform(post("/api/tenants/" + tenantId + "/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));
    }

    @Test
    void getBusinesses_Paginated_Success() throws Exception {
        Business business = new Business();
        business.setId(UUID.randomUUID());
        business.setName("Supermart Downtown");
        business.setBusinessType(BusinessType.SUPERMARKET);
        business.setActive(true);
        business.setTenant(tenant);

        Page<Business> page = new PageImpl<>(List.of(business), PageRequest.of(0, 10), 1);
        when(businessService.getBusinessesByTenant(eq(tenantId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/tenants/" + tenantId + "/businesses")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Businesses fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].name").value("Supermart Downtown"))
                .andExpect(jsonPath("$.data.content[0].businessType").value("SUPERMARKET"))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true))
                .andExpect(jsonPath("$.data.empty").value(false));
    }

    @Test
    void getBusinesses_WithBusinessTypeFilter_Success() throws Exception {
        Business business = new Business();
        business.setId(UUID.randomUUID());
        business.setName("Downtown Pharmacy");
        business.setBusinessType(BusinessType.PHARMACY);
        business.setActive(true);
        business.setTenant(tenant);

        Page<Business> page = new PageImpl<>(List.of(business), PageRequest.of(0, 10), 1);
        when(businessService.getBusinessesByTenantAndType(eq(tenantId), eq(BusinessType.PHARMACY), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/tenants/" + tenantId + "/businesses")
                        .param("businessType", "PHARMACY")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Businesses fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].name").value("Downtown Pharmacy"))
                .andExpect(jsonPath("$.data.content[0].businessType").value("PHARMACY"));
    }

    @Test
    void getBusinessById_Success() throws Exception {
        UUID businessId = UUID.randomUUID();
        Business business = new Business();
        business.setId(businessId);
        business.setName("Supermart Downtown");
        business.setBusinessType(BusinessType.SUPERMARKET);
        business.setActive(true);
        business.setTenant(tenant);

        when(businessService.getBusinessById(tenantId, businessId)).thenReturn(business);

        mockMvc.perform(get("/api/tenants/" + tenantId + "/businesses/" + businessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Business fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(businessId.toString()))
                .andExpect(jsonPath("$.data.name").value("Supermart Downtown"));
    }

    @Test
    void getBusinessById_NotFound_ReturnsFalseWithMessage() throws Exception {
        UUID businessId = UUID.randomUUID();
        when(businessService.getBusinessById(tenantId, businessId))
                .thenThrow(new IllegalArgumentException("Business not found"));

        mockMvc.perform(get("/api/tenants/" + tenantId + "/businesses/" + businessId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Business not found"))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));
    }

    @Test
    void updateBusiness_Success() throws Exception {
        UUID businessId = UUID.randomUUID();
        Business business = new Business();
        business.setId(businessId);
        business.setName("Updated Supermart");
        business.setBusinessType(BusinessType.SUPERMARKET);
        business.setActive(true);
        business.setTenant(tenant);

        when(businessService.updateBusiness(eq(tenantId), eq(businessId), any(BusinessRequest.class)))
                .thenReturn(business);

        String json = """
                {
                    "name": "Updated Supermart",
                    "businessType": "SUPERMARKET"
                }
                """;

        mockMvc.perform(put("/api/tenants/" + tenantId + "/businesses/" + businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Business updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Updated Supermart"));
    }

    @Test
    void updateBusinessStatus_Activate_Success() throws Exception {
        UUID businessId = UUID.randomUUID();
        Business business = new Business();
        business.setId(businessId);
        business.setName("Supermart Downtown");
        business.setActive(true);
        business.setTenant(tenant);

        when(businessService.updateBusinessStatus(tenantId, businessId, true))
                .thenReturn(business);

        mockMvc.perform(patch("/api/tenants/" + tenantId + "/businesses/" + businessId + "/status")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Business activated successfully"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void updateBusinessStatus_Deactivate_Success() throws Exception {
        UUID businessId = UUID.randomUUID();
        Business business = new Business();
        business.setId(businessId);
        business.setName("Supermart Downtown");
        business.setActive(false);
        business.setTenant(tenant);

        when(businessService.updateBusinessStatus(tenantId, businessId, false))
                .thenReturn(business);

        mockMvc.perform(patch("/api/tenants/" + tenantId + "/businesses/" + businessId + "/status")
                        .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Business deactivated successfully"))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void deleteBusiness_Success() throws Exception {
        UUID businessId = UUID.randomUUID();
        Business business = new Business();
        business.setId(businessId);
        business.setName("Supermart Downtown");
        business.setActive(false);
        business.setTenant(tenant);

        when(businessService.deleteBusiness(tenantId, businessId))
                .thenReturn(business);

        mockMvc.perform(delete("/api/tenants/" + tenantId + "/businesses/" + businessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Business deleted successfully"))
                .andExpect(jsonPath("$.data.active").value(false))
                .andExpect(jsonPath("$.data.status").value(false));
    }

    @Test
    void deleteBusiness_NotFound_ReturnsFalseWithMessage() throws Exception {
        UUID businessId = UUID.randomUUID();
        when(businessService.deleteBusiness(tenantId, businessId))
                .thenThrow(new IllegalArgumentException("Business not found"));

        mockMvc.perform(delete("/api/tenants/" + tenantId + "/businesses/" + businessId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Business not found"))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));
    }
}
