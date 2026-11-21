package com.possaas.possaasbackend.productvariant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessAttributeRepository
        extends JpaRepository<BusinessAttribute, UUID> {

    List<BusinessAttribute> findAllByBusinessId(UUID businessId);

    Optional<BusinessAttribute> findByIdAndBusinessId(
            UUID id,
            UUID businessId
    );

    boolean existsByBusinessIdAndCodeIgnoreCase(
            UUID businessId,
            String code
    );
}
