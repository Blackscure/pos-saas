package com.possaas.possaasbackend.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Page<Category> findAllByBusiness_Id(
            UUID businessId,
            Pageable pageable
    );

    Optional<Category> findByIdAndBusiness_Id(
            UUID categoryId,
            UUID businessId
    );

    @Query("""
            SELECT COUNT(c) > 0
            FROM Category c
            WHERE c.business.id = :businessId
              AND LOWER(c.name) = LOWER(:name)
              AND (
                    (:parentCategoryId IS NULL AND c.parentCategory IS NULL)
                    OR c.parentCategory.id = :parentCategoryId
              )
              AND (
                    :excludeCategoryId IS NULL
                    OR c.id <> :excludeCategoryId
              )
            """)
    boolean existsInParent(
            @Param("businessId") UUID businessId,
            @Param("parentCategoryId") UUID parentCategoryId,
            @Param("name") String name,
            @Param("excludeCategoryId") UUID excludeCategoryId
    );

    boolean existsByIdAndBusiness_Id(
            UUID categoryId,
            UUID businessId
    );
}
