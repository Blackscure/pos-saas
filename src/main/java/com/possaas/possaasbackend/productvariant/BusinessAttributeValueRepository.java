package com.possaas.possaasbackend.productvariant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface             BusinessAttributeValueRepository
        extends JpaRepository<BusinessAttributeValue, UUID> {

    List<BusinessAttributeValue> findAllByBusinessIdAndAttributeId(
            UUID businessId,
            UUID attributeId
    );

    Optional<BusinessAttributeValue> findByIdAndBusinessIdAndAttributeId(
            UUID id,
            UUID businessId,
            UUID attributeId
    );

    boolean existsByBusinessIdAndAttributeIdAndValueIgnoreCase(
            UUID businessId,
            UUID attributeId,
            String value
    );
}
