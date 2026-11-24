package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.productvariant.dto.BusinessAttributeValueResponse;
import com.possaas.possaasbackend.productvariant.dto.CreateBusinessAttributeValueRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessAttributeValueService {

    private final BusinessAttributeRepository businessAttributeRepository;
    private final BusinessAttributeValueRepository businessAttributeValueRepository;

    public BusinessAttributeValueResponse createAttributeValue(
            UUID businessId,
            UUID attributeId,
            CreateBusinessAttributeValueRequest request
    ) {
        // Ensure the attribute belongs to this business
        BusinessAttribute attribute = findAttribute(businessId, attributeId);

        String normalizedValue = normalizeValue(request.value());

        // Prevent duplicate values within the same attribute
        if (businessAttributeValueRepository
                .existsByBusinessIdAndAttributeIdAndValueIgnoreCase(
                        businessId,
                        attributeId,
                        normalizedValue
                )) {
            throw new IllegalArgumentException(
                    "This value already exists for the attribute"
            );
        }

        BusinessAttributeValue attributeValue = new BusinessAttributeValue();

        attributeValue.setBusinessId(businessId);
        attributeValue.setAttributeId(attribute.getId());
        attributeValue.setValue(normalizedValue);

        BusinessAttributeValue savedValue =
                businessAttributeValueRepository.save(attributeValue);

        return mapToResponse(savedValue);
    }

    @Transactional(readOnly = true)
    public List<BusinessAttributeValueResponse> getAttributeValues(
            UUID businessId,
            UUID attributeId
    ) {
        // Ensure the attribute belongs to this business
        findAttribute(businessId, attributeId);

        return businessAttributeValueRepository
                .findAllByBusinessIdAndAttributeId(businessId, attributeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BusinessAttributeValueResponse getAttributeValueById(
            UUID businessId,
            UUID attributeId,
            UUID valueId
    ) {
        findAttribute(businessId, attributeId);

        BusinessAttributeValue attributeValue =
                businessAttributeValueRepository
                        .findByIdAndBusinessIdAndAttributeId(
                                valueId,
                                businessId,
                                attributeId
                        )
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Attribute value not found"
                        ));

        return mapToResponse(attributeValue);
    }

    public BusinessAttributeValueResponse updateAttributeValue(
            UUID businessId,
            UUID attributeId,
            UUID valueId,
            CreateBusinessAttributeValueRequest request
    ) {
        findAttribute(businessId, attributeId);

        BusinessAttributeValue attributeValue =
                businessAttributeValueRepository
                        .findByIdAndBusinessIdAndAttributeId(
                                valueId,
                                businessId,
                                attributeId
                        )
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Attribute value not found"
                        ));

        String normalizedValue = normalizeValue(request.value());

        boolean valueExists =
                businessAttributeValueRepository
                        .existsByBusinessIdAndAttributeIdAndValueIgnoreCase(
                                businessId,
                                attributeId,
                                normalizedValue
                        );

        if (valueExists
                && !attributeValue.getValue().equalsIgnoreCase(normalizedValue)) {
            throw new IllegalArgumentException(
                    "This value already exists for the attribute"
            );
        }

        attributeValue.setValue(normalizedValue);

        BusinessAttributeValue updatedValue =
                businessAttributeValueRepository.save(attributeValue);

        return mapToResponse(updatedValue);
    }

    public void deleteAttributeValue(
            UUID businessId,
            UUID attributeId,
            UUID valueId
    ) {
        findAttribute(businessId, attributeId);

        BusinessAttributeValue attributeValue =
                businessAttributeValueRepository
                        .findByIdAndBusinessIdAndAttributeId(
                                valueId,
                                businessId,
                                attributeId
                        )
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Attribute value not found"
                        ));

        businessAttributeValueRepository.delete(attributeValue);
    }

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

    private String normalizeValue(String value) {
        return value.trim();
    }

    private BusinessAttributeValueResponse mapToResponse(
            BusinessAttributeValue attributeValue
    ) {
        return new BusinessAttributeValueResponse(
                attributeValue.getId(),
                attributeValue.getBusinessId(),
                attributeValue.getAttributeId(),
                attributeValue.getValue(),
                attributeValue.getCreatedAt(),
                attributeValue.getUpdatedAt()
        );
    }
}
