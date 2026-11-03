package com.possaas.possaasbackend.branch.dto;

import com.possaas.possaasbackend.branch.Branch;

import java.util.UUID;

public record BranchResponse(
        UUID id,
        UUID businessId,
        String name,
        String code,
        String phone,
        String email,
        String address,
        boolean active
) {

    public static BranchResponse fromEntity(Branch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getBusiness().getId(),
                branch.getName(),
                branch.getCode(),
                branch.getPhone(),
                branch.getEmail(),
                branch.getAddress(),
                branch.isActive()
        );
    }
}