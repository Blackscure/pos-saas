package com.possaas.possaasbackend.productvariant;

import jakarta.persistence.EntityNotFoundException;
import com.possaas.possaasbackend.product.Product;
import com.possaas.possaasbackend.product.ProductRepository;
import com.possaas.possaasbackend.productvariant.dto.CreateProductVariantRequest;
import com.possaas.possaasbackend.productvariant.dto.UpdateProductVariantRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    public ProductVariant createVariant(
            UUID businessId,
            UUID productId,
            CreateProductVariantRequest request
    ) {
        Product product = getProduct(businessId, productId);

        validateSku(businessId, request.sku());
        validateBarcode(businessId, request.barcode());

        ProductVariant variant = new ProductVariant();

        variant.setId(UUID.randomUUID());
        variant.setBusinessId(businessId);
        variant.setProductId(productId);
        variant.setSku(request.sku().trim());
        variant.setBarcode(normalizeBarcode(request.barcode()));
        variant.setName(request.name());
        variant.setPurchasePrice(request.purchasePrice());
        variant.setSellingPrice(request.sellingPrice());
        variant.setActive(true);

        return productVariantRepository.save(variant);
    }

    @Transactional(readOnly = true)
    public List<ProductVariant> getProductVariants(
            UUID businessId,
            UUID productId
    ) {
        getProduct(businessId, productId);

        return productVariantRepository.findAllByBusinessIdAndProductId(
                businessId,
                productId
        );
    }

    @Transactional(readOnly = true)
    public ProductVariant getVariantById(
            UUID businessId,
            UUID productId,
            UUID variantId
    ) {
        getProduct(businessId, productId);

        return productVariantRepository.findByIdAndBusinessIdAndProductId(
                        variantId,
                        businessId,
                        productId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException("Product variant not found")
                );
    }

    public ProductVariant updateVariant(
            UUID businessId,
            UUID productId,
            UUID variantId,
            UpdateProductVariantRequest request
    ) {
        ProductVariant variant = getVariantById(
                businessId,
                productId,
                variantId
        );

        if (request.sku() != null && !request.sku().isBlank()) {
            String newSku = request.sku().trim();

            if (!newSku.equalsIgnoreCase(variant.getSku())) {
                validateSku(businessId, newSku);
                variant.setSku(newSku);
            }
        }

        if (request.barcode() != null) {
            String newBarcode = normalizeBarcode(request.barcode());

            if (!newBarcodeEquals(newBarcode, variant.getBarcode())) {
                validateBarcode(businessId, newBarcode);
                variant.setBarcode(newBarcode);
            }
        }

        if (request.name() != null) {
            variant.setName(request.name().trim());
        }

        if (request.purchasePrice() != null) {
            validatePrice(request.purchasePrice(), "Purchase price");
            variant.setPurchasePrice(request.purchasePrice());
        }

        if (request.sellingPrice() != null) {
            validatePrice(request.sellingPrice(), "Selling price");
            variant.setSellingPrice(request.sellingPrice());
        }

        return productVariantRepository.save(variant);
    }

    public ProductVariant updateVariantStatus(
            UUID businessId,
            UUID productId,
            UUID variantId,
            boolean active
    ) {
        ProductVariant variant = getVariantById(
                businessId,
                productId,
                variantId
        );

        variant.setActive(active);

        return productVariantRepository.save(variant);
    }

    private Product getProduct(UUID businessId, UUID productId) {
        return productRepository.findByIdAndBusiness_Id(
                        productId,
                        businessId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException("Product not found")
                );
    }

    private void validateSku(UUID businessId, String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU is required");
        }

        if (productVariantRepository.existsByBusinessIdAndSkuIgnoreCase(
                businessId,
                sku.trim()
        )) {
            throw new IllegalArgumentException(
                    "A variant with this SKU already exists in this business"
            );
        }
    }

    private void validateBarcode(UUID businessId, String barcode) {
        String normalizedBarcode = normalizeBarcode(barcode);

        if (normalizedBarcode == null) {
            return;
        }

        if (productVariantRepository.existsByBusinessIdAndBarcodeIgnoreCase(
                businessId,
                normalizedBarcode
        )) {
            throw new IllegalArgumentException(
                    "A variant with this barcode already exists in this business"
            );
        }
    }

    private String normalizeBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return null;
        }

        return barcode.trim();
    }

    private boolean newBarcodeEquals(String first, String second) {
        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.equalsIgnoreCase(second);
    }

    private void validatePrice(BigDecimal price, String fieldName) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative"
            );
        }
    }
}
