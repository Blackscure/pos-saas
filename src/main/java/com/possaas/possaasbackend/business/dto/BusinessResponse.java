package com.possaas.possaasbackend.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessType;
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
    private UUID tenantId;
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

    public static BusinessResponse fromEntity(Business business) {
        if (business == null) {
            return null;
        }
        return BusinessResponse.builder()
                .id(business.getId())
                .tenantId(business.getTenant() != null ? business.getTenant().getId() : null)
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
