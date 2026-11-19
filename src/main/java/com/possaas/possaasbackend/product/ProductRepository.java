package com.possaas.possaasbackend.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findAllByBusiness_Id(UUID businessId, Pageable pageable);

    Optional<Product> findByIdAndBusiness_Id(UUID productId, UUID businessId);

    boolean existsByBusiness_IdAndSkuIgnoreCase(
            UUID businessId,
            String sku
    );

    boolean existsByBusiness_IdAndBarcodeIgnoreCase(
            UUID businessId,
            String barcode
    );

    boolean existsByBusiness_IdAndSkuIgnoreCaseAndIdNot(
            UUID businessId,
            String sku,
            UUID productId
    );

    boolean existsByBusiness_IdAndBarcodeIgnoreCaseAndIdNot(
            UUID businessId,
            String barcode,
            UUID productId
    );
}
