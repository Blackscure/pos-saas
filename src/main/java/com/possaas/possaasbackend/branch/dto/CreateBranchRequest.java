package com.possaas.possaasbackend.branch.dto;

public record CreateBranchRequest(
        String name,
        String code,
        String phone,
        String email,
        String address
) {
}