
package com.possaas.possaasbackend.supplier;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.security.AuthenticatedUser;
import com.possaas.possaasbackend.supplier.dto.CreateSupplierRequest;
import com.possaas.possaasbackend.supplier.dto.SupplierResponse;
import com.possaas.possaasbackend.supplier.dto.UpdateSupplierRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    // =========================================================
    // CREATE SUPPLIER
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAuthority('SUPPLIER_CREATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<SupplierResponse> createSupplier(
            @PathVariable UUID businessId,
            @Valid @RequestBody CreateSupplierRequest request,
            Authentication authentication
    ) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        Supplier supplier = supplierService.createSupplier(
                user.tenantId(),
                businessId,
                request
        );

        return ApiResponse.success(
                "Supplier created successfully",
                SupplierResponse.from(supplier)
        );
    }

    // =========================================================
    // GET SUPPLIERS
    // =========================================================

    @GetMapping
    @PreAuthorize("hasAuthority('SUPPLIER_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<PageResponse<SupplierResponse>> getSuppliers(
            @PathVariable UUID businessId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        Pageable pageable = PageRequest.of(page, size);

        Page<Supplier> suppliers = supplierService.getSuppliers(
                user.tenantId(),
                businessId,
                search,
                pageable
        );

        Page<SupplierResponse> responsePage =
                suppliers.map(SupplierResponse::from);

        return ApiResponse.success(
                "Suppliers retrieved successfully",
                PageResponse.of(responsePage)
        );
    }

    // =========================================================
    // GET SUPPLIER BY ID
    // =========================================================

    @GetMapping("/{supplierId}")
    @PreAuthorize("hasAuthority('SUPPLIER_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<SupplierResponse> getSupplierById(
            @PathVariable UUID businessId,
            @PathVariable UUID supplierId,
            Authentication authentication
    ) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        Supplier supplier = supplierService.getSupplierById(
                user.tenantId(),
                businessId,
                supplierId
        );

        return ApiResponse.success(
                "Supplier retrieved successfully",
                SupplierResponse.from(supplier)
        );
    }

    // =========================================================
    // UPDATE SUPPLIER
    // =========================================================

    @PutMapping("/{supplierId}")
    @PreAuthorize("hasAuthority('SUPPLIER_UPDATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<SupplierResponse> updateSupplier(
            @PathVariable UUID businessId,
            @PathVariable UUID supplierId,
            @Valid @RequestBody UpdateSupplierRequest request,
            Authentication authentication
    ) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        Supplier supplier = supplierService.updateSupplier(
                user.tenantId(),
                businessId,
                supplierId,
                request
        );

        return ApiResponse.success(
                "Supplier updated successfully",
                SupplierResponse.from(supplier)
        );
    }

    // =========================================================
    // UPDATE SUPPLIER STATUS
    // =========================================================

    @PatchMapping("/{supplierId}/status")
    @PreAuthorize("hasAuthority('SUPPLIER_UPDATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<SupplierResponse> updateSupplierStatus(
            @PathVariable UUID businessId,
            @PathVariable UUID supplierId,
            @RequestParam boolean active,
            Authentication authentication
    ) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        Supplier supplier = supplierService.updateSupplierStatus(
                user.tenantId(),
                businessId,
                supplierId,
                active
        );

        return ApiResponse.success(
                "Supplier status updated successfully",
                SupplierResponse.from(supplier)
        );
    }

    // =========================================================
    // DELETE SUPPLIER (SOFT DELETE)
    // =========================================================

    @DeleteMapping("/{supplierId}")
    @PreAuthorize("hasAuthority('SUPPLIER_DELETE') and #businessId == authentication.principal.businessId")
    public ApiResponse<SupplierResponse> deleteSupplier(
            @PathVariable UUID businessId,
            @PathVariable UUID supplierId,
            Authentication authentication
    ) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        Supplier supplier = supplierService.deleteSupplier(
                user.tenantId(),
                businessId,
                supplierId
        );

        return ApiResponse.success(
                "Supplier deactivated successfully",
                SupplierResponse.from(supplier)
        );
    }
}
