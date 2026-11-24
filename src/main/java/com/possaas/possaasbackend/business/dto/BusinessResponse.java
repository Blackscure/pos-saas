package com.possaas.possaasbackend.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessType;
import com.possaas.possaasbackend.tenant.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessResponse {

    private UUID id;
    private TenantSummary tenant;
    private String name;
    private BusinessType businessType;
    private String registrationNumber;
    private String phone;
    private String email;
    private String address;
    private String currency;
    private boolean taxEnabled;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonProperty("status")
    public boolean getStatus() {
        return active;
    }

    /**
     * Nested tenant information returned alongside a business.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantSummary {

        private UUID id;
        private String name;
        private String code;
        private String email;
        private boolean active;

        public static TenantSummary fromEntity(Tenant tenant) {
            if (tenant == null) {
                return null;
            }

            return TenantSummary.builder()
                    .id(tenant.getId())
                    .name(tenant.getName())
                    .code(tenant.getCode())
                    .email(tenant.getEmail())
                    .active(tenant.isActive())
                    .build();
        }
    }

    public static BusinessResponse fromEntity(Business business) {
        if (business == null) {
            return null;
        }

        return BusinessResponse.builder()
                .id(business.getId())
                .tenant(TenantSummary.fromEntity(business.getTenant()))
                .name(business.getName())
                .businessType(business.getBusinessType())
                .registrationNumber(business.getRegistrationNumber())
                .phone(business.getPhone())
                .email(business.getEmail())
                .address(business.getAddress())
                .currency(business.getCurrency())
                .taxEnabled(business.isTaxEnabled())
                .active(business.isActive())
                .createdAt(business.getCreatedAt())
                .updatedAt(business.getUpdatedAt())
                .build();
    }
}