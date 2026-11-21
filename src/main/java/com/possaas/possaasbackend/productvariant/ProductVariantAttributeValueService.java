package com.possaas.possaasbackend.productvariant;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductVariantAttributeValueService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final BusinessAttributeValueRepository businessAttributeValueRepository;
    private final ProductVariantAttributeValueRepository productVariantAttributeValueRepository;

    public ProductVariantAttributeValueService(
            ProductVariantRepository productVariantRepository,
            ProductAttributeRepository productAttributeRepository,
            BusinessAttributeValueRepository businessAttributeValueRepository,
            ProductVariantAttributeValueRepository productVariantAttributeValueRepository) {

        this.productVariantRepository = productVariantRepository;
        this.productAttributeRepository = productAttributeRepository;
        this.businessAttributeValueRepository = businessAttributeValueRepository;
        this.productVariantAttributeValueRepository = productVariantAttributeValueRepository;
    }

    public ProductVariantAttributeValue setAttributeValue(
            UUID businessId,
            UUID productId,
            UUID variantId,
            UUID productAttributeId,
            UUID attributeValueId) {

        // Verify the variant belongs to this business and product
        ProductVariant variant = productVariantRepository
                .findByIdAndBusinessIdAndProductId(
                        variantId, businessId, productId
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product variant not found"
                ));

        // Verify the attribute is assigned to this product
        ProductAttribute productAttribute = productAttributeRepository
                .findByIdAndBusinessIdAndProductId(
                        productAttributeId, businessId, productId
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product attribute not found"
                ));

        // Verify the selected value belongs to the attribute
        BusinessAttributeValue attributeValue = businessAttributeValueRepository
                .findByIdAndBusinessIdAndAttributeId(
                        attributeValueId,
                        businessId,
                        productAttribute.getAttributeId()
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        "Attribute value not found"
                ));

        // Check if a value already exists for this variant and attribute
        ProductVariantAttributeValue variantAttributeValue =
                productVariantAttributeValueRepository
                        .findAllByBusinessIdAndProductIdAndProductVariantId(
                                businessId, productId, variantId
                        )
                        .stream()
                        .filter(item -> item.getProductAttributeId().equals(productAttributeId))
                        .findFirst()
                        .orElseGet(ProductVariantAttributeValue::new);

        // Populate entity
        if (variantAttributeValue.getId() == null) {
            variantAttributeValue.setId(UUID.randomUUID());
        }

        variantAttributeValue.setBusinessId(businessId);
        variantAttributeValue.setProductId(productId);
        variantAttributeValue.setProductVariantId(variantId);
        variantAttributeValue.setProductAttributeId(productAttributeId);
        variantAttributeValue.setAttributeId(productAttribute.getAttributeId());
        variantAttributeValue.setAttributeValueId(attributeValue.getId());

        return productVariantAttributeValueRepository.save(variantAttributeValue);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantAttributeValue> getVariantAttributeValues(
            UUID businessId,
            UUID productId,
            UUID variantId) {

        // Verify variant ownership
        productVariantRepository
                .findByIdAndBusinessIdAndProductId(
                        variantId, businessId, productId
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product variant not found"
                ));

        return productVariantAttributeValueRepository
                .findAllByBusinessIdAndProductIdAndProductVariantId(
                        businessId, productId, variantId
                );
    }

    public void removeAttributeValue(
            UUID businessId,
            UUID productId,
            UUID variantId,
            UUID productAttributeId) {

        // Verify variant ownership
        productVariantRepository
                .findByIdAndBusinessIdAndProductId(
                        variantId, businessId, productId
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product variant not found"
                ));

        // Find the attribute value assigned to this variant
        ProductVariantAttributeValue variantAttributeValue =
                productVariantAttributeValueRepository
                        .findAllByBusinessIdAndProductIdAndProductVariantId(
                                businessId, productId, variantId
                        )
                        .stream()
                        .filter(item -> item.getProductAttributeId().equals(productAttributeId))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Variant attribute value not found"
                        ));

        productVariantAttributeValueRepository.delete(variantAttributeValue);
    }
}
