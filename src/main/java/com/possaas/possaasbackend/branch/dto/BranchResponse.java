package com.possaas.possaasbackend.branch.dto;

import com.possaas.possaasbackend.branch.Branch;
import com.possaas.possaasbackend.business.dto.BusinessResponse;

import java.util.UUID;

public record BranchResponse(
        UUID id,
        BusinessResponse business,
        String name,
        String code,
        String phone,
        String email,
        String address,
        boolean active
) {

    public static BranchResponse fromEntity(Branch branch) {
        if (branch == null) {
            return null;
        }

        return new BranchResponse(
                branch.getId(),
                BusinessResponse.fromEntity(branch.getBusiness()),
                branch.getName(),
                branch.getCode(),
                branch.getPhone(),
                branch.getEmail(),
                branch.getAddress(),
                branch.isActive()
        );
    }
}