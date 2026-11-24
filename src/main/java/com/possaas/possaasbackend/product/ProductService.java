
package com.possaas.possaasbackend.product;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.category.Category;
import com.possaas.possaasbackend.category.CategoryRepository;
import com.possaas.possaasbackend.product.dto.CreateProductRequest;
import com.possaas.possaasbackend.product.dto.UpdateProductRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            BusinessRepository businessRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.businessRepository = businessRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product createProduct(
            UUID tenantId,
            UUID businessId,
            CreateProductRequest request
    ) {
        Business business = getBusinessByTenant(tenantId, businessId);

        validateBusinessIsActive(business);

        String sku = request.sku().trim();
        String barcode = normalizeBarcode(request.barcode());

        validateUniqueSku(businessId, sku, null);
        validateUniqueBarcode(businessId, barcode, null);

        if (request.categoryId() == null) {
            throw new IllegalArgumentException("Category ID is required");
        }

        Category category = getCategory(businessId, request.categoryId());
        validateCategoryIsActive(category);

        Product product = new Product();

        product.setBusiness(business);
        product.setCategory(category);
        product.setSku(sku);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setBarcode(barcode);
        product.setImageUrl(request.imageUrl());
        product.setPurchasePrice(request.purchasePrice());
        product.setSellingPrice(request.sellingPrice());
        product.setTaxRate(request.taxRate());
        product.setTaxInclusive(request.taxInclusive());
        product.setActive(true);

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProducts(
            UUID tenantId,
            UUID businessId,
            String search,
            String name,
            String sku,
            String barcode,
            UUID categoryId,
            Boolean active,
            Pageable pageable
    ) {
        getBusinessByTenant(tenantId, businessId);

        String normalizedSearch = normalizeSearchTerm(search);
        String normalizedName = normalizeSearchTerm(name);
        String normalizedSku = normalizeSearchTerm(sku);
        String normalizedBarcode = normalizeSearchTerm(barcode);

        Specification<Product> specification = (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Always restrict products to the requested business.
            predicates.add(
                    criteriaBuilder.equal(
                            root.get("business").get("id"),
                            businessId
                    )
            );

            if (categoryId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("category").get("id"),
                                categoryId
                        )
                );
            }

            if (active != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("active"),
                                active
                        )
                );
            }

            if (normalizedName != null) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                contains(normalizedName)
                        )
                );
            }

            if (normalizedSku != null) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("sku")),
                                contains(normalizedSku)
                        )
                );
            }

            if (normalizedBarcode != null) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("barcode")),
                                contains(normalizedBarcode)
                        )
                );
            }

            if (normalizedSearch != null) {
                String searchPattern = contains(normalizedSearch);

                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("name")),
                                        searchPattern
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("sku")),
                                        searchPattern
                                ),
                                criteriaBuilder.and(
                                        criteriaBuilder.isNotNull(
                                                root.get("barcode")
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(root.get("barcode")),
                                                searchPattern
                                        )
                                )
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };

        return productRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProducts(
            UUID tenantId,
            UUID businessId,
            Pageable pageable
    ) {
        return getProducts(
                tenantId,
                businessId,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Product getProductById(
            UUID tenantId,
            UUID businessId,
            UUID productId
    ) {
        getBusinessByTenant(tenantId, businessId);

        return productRepository.findByIdAndBusiness_Id(
                productId,
                businessId
        ).orElseThrow(() ->
                new IllegalArgumentException("Product not found")
        );
    }

    public Product updateProduct(
            UUID tenantId,
            UUID businessId,
            UUID productId,
            UpdateProductRequest request
    ) {
        Business business = getBusinessByTenant(tenantId, businessId);

        validateBusinessIsActive(business);

        Product product = getProductById(
                tenantId,
                businessId,
                productId
        );

        String sku = request.sku().trim();
        String barcode = normalizeBarcode(request.barcode());

        validateUniqueSku(businessId, sku, productId);
        validateUniqueBarcode(businessId, barcode, productId);

        Category category = getCategory(businessId, request.categoryId());
        validateCategoryIsActive(category);

        product.setCategory(category);
        product.setSku(sku);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setBarcode(barcode);
        product.setImageUrl(request.imageUrl());
        product.setPurchasePrice(request.purchasePrice());
        product.setSellingPrice(request.sellingPrice());
        product.setTaxRate(request.taxRate());
        product.setTaxInclusive(request.taxInclusive());

        return productRepository.save(product);
    }

    public Product updateProductStatus(
            UUID tenantId,
            UUID businessId,
            UUID productId,
            boolean active
    ) {
        Business business = getBusinessByTenant(tenantId, businessId);

        validateBusinessIsActive(business);

        Product product = getProductById(
                tenantId,
                businessId,
                productId
        );

        product.setActive(active);

        return productRepository.save(product);
    }

    public Product deleteProduct(
            UUID tenantId,
            UUID businessId,
            UUID productId
    ) {
        return updateProductStatus(
                tenantId,
                businessId,
                productId,
                false
        );
    }

    private Business getBusinessByTenant(
            UUID tenantId,
            UUID businessId
    ) {
        return businessRepository.findByIdAndTenantId(
                businessId,
                tenantId
        ).orElseThrow(() ->
                new IllegalArgumentException("Business not found")
        );
    }

    private Category getCategory(
            UUID businessId,
            UUID categoryId
    ) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findByIdAndBusiness_Id(
                categoryId,
                businessId
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Category not found in this business"
                )
        );
    }

    private void validateBusinessIsActive(Business business) {
        if (!business.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot manage products under an inactive business"
            );
        }
    }

    private void validateCategoryIsActive(Category category) {
        if (category != null && !category.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot assign a product to an inactive category"
            );
        }
    }

    private void validateUniqueSku(
            UUID businessId,
            String sku,
            UUID excludeProductId
    ) {
        boolean exists = excludeProductId == null
                ? productRepository.existsByBusiness_IdAndSkuIgnoreCase(
                businessId,
                sku
        )
                : productRepository.existsByBusiness_IdAndSkuIgnoreCaseAndIdNot(
                businessId,
                sku,
                excludeProductId
        );

        if (exists) {
            throw new IllegalArgumentException(
                    "A product with this SKU already exists in this business"
            );
        }
    }

    private void validateUniqueBarcode(
            UUID businessId,
            String barcode,
            UUID excludeProductId
    ) {
        if (barcode == null) {
            return;
        }

        boolean exists = excludeProductId == null
                ? productRepository.existsByBusiness_IdAndBarcodeIgnoreCase(
                businessId,
                barcode
        )
                : productRepository.existsByBusiness_IdAndBarcodeIgnoreCaseAndIdNot(
                businessId,
                barcode,
                excludeProductId
        );

        if (exists) {
            throw new IllegalArgumentException(
                    "A product with this barcode already exists in this business"
            );
        }
    }

    private String normalizeBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return null;
        }

        return barcode.trim();
    }

    private String normalizeSearchTerm(String term) {
        if (term == null || term.isBlank()) {
            return null;
        }

        return term.trim();
    }

    private String contains(String value) {
        return "%" + value.toLowerCase(Locale.ROOT) + "%";
    }
}
