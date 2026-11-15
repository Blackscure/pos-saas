package com.possaas.possaasbackend.category;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.category.dto.CreateCategoryRequest;
import com.possaas.possaasbackend.category.dto.UpdateCategoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BusinessRepository businessRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            BusinessRepository businessRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.businessRepository = businessRepository;
    }

    public Category createCategory(
            UUID tenantId,
            UUID businessId,
            CreateCategoryRequest request
    ) {
        Business business = getBusinessByTenant(tenantId, businessId);

        if (!business.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot create categories under an inactive business"
            );
        }

        String name = request.name().trim();

        Category parentCategory = getParentCategory(
                businessId,
                request.parentCategoryId()
        );

        validateParentIsActive(parentCategory);

        if (categoryRepository.existsInParent(
                businessId,
                request.parentCategoryId(),
                name,
                null
        )) {
            throw new IllegalArgumentException(
                    "A category with this name already exists under this parent"
            );
        }

        Category category = new Category();
        category.setBusiness(business);
        category.setParentCategory(parentCategory);
        category.setName(name);
        category.setDescription(request.description());
        category.setActive(true);

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Page<Category> getCategories(
            UUID tenantId,
            UUID businessId,
            Pageable pageable
    ) {
        getBusinessByTenant(tenantId, businessId);

        return categoryRepository.findAllByBusiness_Id(
                businessId,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(
            UUID tenantId,
            UUID businessId,
            UUID categoryId
    ) {
        getBusinessByTenant(tenantId, businessId);

        return categoryRepository.findByIdAndBusiness_Id(
                categoryId,
                businessId
        ).orElseThrow(() ->
                new IllegalArgumentException("Category not found")
        );
    }

    public Category updateCategory(
            UUID tenantId,
            UUID businessId,
            UUID categoryId,
            UpdateCategoryRequest request
    ) {
        Business business = getBusinessByTenant(tenantId, businessId);

        if (!business.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot update categories under an inactive business"
            );
        }

        Category category = getCategoryById(
                tenantId,
                businessId,
                categoryId
        );

        Category newParent = getParentCategory(
                businessId,
                request.parentCategoryId()
        );

        validateParentIsActive(newParent);
        validateNoCircularHierarchy(category, newParent);

        String newName = request.name().trim();

        if (categoryRepository.existsInParent(
                businessId,
                request.parentCategoryId(),
                newName,
                categoryId
        )) {
            throw new IllegalArgumentException(
                    "A category with this name already exists under this parent"
            );
        }

        category.setName(newName);
        category.setDescription(request.description());
        category.setParentCategory(newParent);

        return categoryRepository.save(category);
    }

    public Category updateCategoryStatus(
            UUID tenantId,
            UUID businessId,
            UUID categoryId,
            boolean active
    ) {
        Business business = getBusinessByTenant(tenantId, businessId);

        if (!business.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot update categories under an inactive business"
            );
        }

        Category category = getCategoryById(
                tenantId,
                businessId,
                categoryId
        );

        if (active) {
            validateParentIsActive(category.getParentCategory());
        }

        category.setActive(active);

        return categoryRepository.save(category);
    }

    public Category deleteCategory(
            UUID tenantId,
            UUID businessId,
            UUID categoryId
    ) {
        return updateCategoryStatus(
                tenantId,
                businessId,
                categoryId,
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

    private Category getParentCategory(
            UUID businessId,
            UUID parentCategoryId
    ) {
        if (parentCategoryId == null) {
            return null;
        }

        return categoryRepository.findByIdAndBusiness_Id(
                parentCategoryId,
                businessId
        ).orElseThrow(() ->
                new IllegalArgumentException("Parent category not found")
        );
    }

    private void validateParentIsActive(Category parentCategory) {
        if (parentCategory != null && !parentCategory.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot assign a category under an inactive parent category"
            );
        }
    }

    private void validateNoCircularHierarchy(
            Category category,
            Category newParent
    ) {
        if (newParent == null) {
            return;
        }

        Set<UUID> visited = new HashSet<>();
        Category current = newParent;

        while (current != null) {
            if (current.getId().equals(category.getId())) {
                throw new IllegalArgumentException(
                        "A category cannot be its own parent or descendant"
                );
            }

            if (!visited.add(current.getId())) {
                throw new IllegalArgumentException(
                        "An existing circular category hierarchy was detected"
                );
            }

            current = current.getParentCategory();
        }
    }
}
