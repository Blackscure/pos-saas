package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.productvariant.dto.BusinessAttributeResponse;
import com.possaas.possaasbackend.productvariant.dto.CreateBusinessAttributeRequest;
import com.possaas.possaasbackend.productvariant.dto.UpdateBusinessAttributeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessAttributeService {

    private final BusinessAttributeRepository businessAttributeRepository;

    /**
     * Create a new reusable attribute for a business.
     */
    public BusinessAttributeResponse createAttribute(
            UUID businessId,
            CreateBusinessAttributeRequest request
    ) {
        String normalizedCode = normalizeCode(request.code());

        if (businessAttributeRepository.existsByBusinessIdAndCodeIgnoreCase(
                businessId,
                normalizedCode
        )) {
            throw new IllegalArgumentException(
                    "Attribute code already exists in this business"
            );
        }

        BusinessAttribute attribute = new BusinessAttribute();
        attribute.setBusinessId(businessId);
        attribute.setName(request.name().trim());
        attribute.setCode(normalizedCode);

        BusinessAttribute savedAttribute =
                businessAttributeRepository.save(attribute);

        return mapToResponse(savedAttribute);
    }

    /**
     * Retrieve all attributes belonging to a business.
     */
    @Transactional(readOnly = true)
    public List<BusinessAttributeResponse> getAllAttributes(
            UUID businessId
    ) {
        return businessAttributeRepository
                .findAllByBusinessId(businessId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieve a single attribute belonging to a business.
     */
    @Transactional(readOnly = true)
    public BusinessAttributeResponse getAttributeById(
            UUID businessId,
            UUID attributeId
    ) {
        BusinessAttribute attribute = findAttribute(businessId, attributeId);

        return mapToResponse(attribute);
    }

    /**
     * Update an existing business attribute.
     */
    public BusinessAttributeResponse updateAttribute(
            UUID businessId,
            UUID attributeId,
            UpdateBusinessAttributeRequest request
    ) {
        BusinessAttribute attribute = findAttribute(businessId, attributeId);

        String normalizedCode = normalizeCode(request.code());

        boolean codeExists =
                businessAttributeRepository
                        .existsByBusinessIdAndCodeIgnoreCase(
                                businessId,
                                normalizedCode
                        );

        if (codeExists && !attribute.getCode().equalsIgnoreCase(normalizedCode)) {
            throw new IllegalArgumentException(
                    "Attribute code already exists in this business"
            );
        }

        attribute.setName(request.name().trim());
        attribute.setCode(normalizedCode);

        BusinessAttribute updatedAttribute =
                businessAttributeRepository.save(attribute);

        return mapToResponse(updatedAttribute);
    }

    /**
     * Find an attribute while enforcing business ownership.
     */
    private BusinessAttribute findAttribute(
            UUID businessId,
            UUID attributeId
    ) {
        return businessAttributeRepository
                .findByIdAndBusinessId(attributeId, businessId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Business attribute not found"
                ));
    }

    /**
     * Normalize attribute codes.
     */
    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    /**
     * Convert entity into response DTO.
     */
    private BusinessAttributeResponse mapToResponse(
            BusinessAttribute attribute
    ) {
        return new BusinessAttributeResponse(
                attribute.getId(),
                attribute.getBusinessId(),
                attribute.getName(),
                attribute.getCode(),
                attribute.getCreatedAt(),
                attribute.getUpdatedAt()
        );
    }
}
