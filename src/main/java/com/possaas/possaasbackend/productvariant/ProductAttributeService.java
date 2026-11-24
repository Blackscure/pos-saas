package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.product.Product;
import com.possaas.possaasbackend.product.ProductRepository;
import com.possaas.possaasbackend.productvariant.dto.AssignProductAttributeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductAttributeService {

    private final ProductRepository productRepository;
    private final BusinessAttributeRepository businessAttributeRepository;
    private final ProductAttributeRepository productAttributeRepository;

    /**
     * Assigns a business attribute to a product.
     */
    public ProductAttribute assignAttribute(
            UUID businessId,
            UUID productId,
            AssignProductAttributeRequest request
    ) {
        // Ensure the product belongs to this business
        Product product = productRepository
                .findByIdAndBusiness_Id(productId, businessId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found"
                ));

        // Ensure the attribute belongs to this business
        BusinessAttribute attribute = businessAttributeRepository
                .findByIdAndBusinessId(request.attributeId(), businessId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Business attribute not found"
                ));

        // Prevent assigning the same attribute more than once
        if (productAttributeRepository
                .existsByBusinessIdAndProductIdAndAttributeId(
                        businessId,
                        product.getId(),
                        attribute.getId()
                )) {
            throw new IllegalArgumentException(
                    "This attribute is already assigned to the product"
            );
        }

        ProductAttribute productAttribute = new ProductAttribute();

        productAttribute.setBusinessId(businessId);
        productAttribute.setProductId(product.getId());
        productAttribute.setAttributeId(attribute.getId());

        return productAttributeRepository.save(productAttribute);
    }

    /**
     * Retrieves all attributes assigned to a product.
     */
    @Transactional(readOnly = true)
    public List<ProductAttribute> getProductAttributes(
            UUID businessId,
            UUID productId
    ) {
        // Verify product ownership
        productRepository
                .findByIdAndBusiness_Id(productId, businessId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found"
                ));

        return productAttributeRepository
                .findAllByBusinessIdAndProductId(businessId, productId);
    }

    /**
     * Retrieves a specific attribute assignment for a product.
     */
    @Transactional(readOnly = true)
    public ProductAttribute getProductAttributeById(
            UUID businessId,
            UUID productId,
            UUID productAttributeId
    ) {
        // Verify product ownership
        productRepository
                .findByIdAndBusiness_Id(productId, businessId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found"
                ));

        return productAttributeRepository
                .findByIdAndBusinessIdAndProductId(
                        productAttributeId,
                        businessId,
                        productId
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product attribute assignment not found"
                ));
    }

    /**
     * Removes an attribute assignment from a product.
     */
    public void removeAttribute(
            UUID businessId,
            UUID productId,
            UUID productAttributeId
    ) {
        ProductAttribute productAttribute =
                getProductAttributeById(
                        businessId,
                        productId,
                        productAttributeId
                );

        productAttributeRepository.delete(productAttribute);
    }
}
