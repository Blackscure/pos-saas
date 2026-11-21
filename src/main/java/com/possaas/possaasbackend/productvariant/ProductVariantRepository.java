package com.possaas.possaasbackend.productvariant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findAllByBusinessIdAndProductId(
            UUID businessId,
            UUID productId
    );

    Optional<ProductVariant> findByIdAndBusinessIdAndProductId(
            UUID id,
            UUID businessId,
            UUID productId
    );

    boolean existsByBusinessIdAndSkuIgnoreCase(
            UUID businessId,
            String sku
    );

    boolean existsByBusinessIdAndBarcodeIgnoreCase(
            UUID businessId,
            String barcode
    );

    boolean existsByIdAndBusinessIdAndProductIdAndActiveTrue(
            UUID id,
            UUID businessId,
            UUID productId
    );
}
