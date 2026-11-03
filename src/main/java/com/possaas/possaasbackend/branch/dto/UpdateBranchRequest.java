package com.possaas.possaasbackend.branch.dto;

public record UpdateBranchRequest(
        String name,
        String code,
        String phone,
        String email,
        String address
) {
}