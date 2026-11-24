package com.possaas.possaasbackend.productvariant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariantAttributeValueRepository
        extends JpaRepository<ProductVariantAttributeValue, UUID> {

    List<ProductVariantAttributeValue> findAllByBusinessIdAndProductIdAndProductVariantId(
            UUID businessId,
            UUID productId,
            UUID productVariantId
    );

    Optional<ProductVariantAttributeValue> findByIdAndBusinessIdAndProductIdAndProductVariantId(
            UUID id,
            UUID businessId,
            UUID productId,
            UUID productVariantId
    );

    boolean existsByProductVariantIdAndBusinessIdAndAttributeId(
            UUID productVariantId,
            UUID businessId,
            UUID attributeId
    );

    boolean existsByProductVariantIdAndAttributeValueId(
            UUID productVariantId,
            UUID attributeValueId
    );

    void deleteAllByBusinessIdAndProductIdAndProductVariantId(
            UUID businessId,
            UUID productId,
            UUID productVariantId
    );
}
