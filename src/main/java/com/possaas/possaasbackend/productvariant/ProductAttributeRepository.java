package com.possaas.possaasbackend.productvariant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductAttributeRepository
        extends JpaRepository<ProductAttribute, UUID> {

    List<ProductAttribute> findAllByBusinessIdAndProductId(
            UUID businessId,
            UUID productId
    );

    Optional<ProductAttribute> findByIdAndBusinessIdAndProductId(
            UUID id,
            UUID businessId,
            UUID productId
    );

    boolean existsByBusinessIdAndProductIdAndAttributeId(
            UUID businessId,
            UUID productId,
            UUID attributeId
    );
}
