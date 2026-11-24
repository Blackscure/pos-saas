package com.possaas.possaasbackend.category;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.category.dto.CreateCategoryRequest;
import com.possaas.possaasbackend.category.dto.UpdateCategoryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private CategoryService categoryService;

    private UUID tenantId;
    private UUID businessId;
    private UUID categoryId;
    private Business business;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        business = new Business();
        business.setId(businessId);
        business.setActive(true);
    }

    private void mockBusinessExists() {
        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));
    }

    private Category buildCategory(UUID id, String name, Business business, Category parent, boolean active) {
        Category category = new Category();
        ReflectionTestUtils.setField(category, "id", id);
        ReflectionTestUtils.setField(category, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(category, "updatedAt", LocalDateTime.now());
        category.setName(name);
        category.setBusiness(business);
        category.setParentCategory(parent);
        category.setActive(active);
        return category;
    }

    // =========================================================================
    // CREATE CATEGORY
    // =========================================================================
    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create root category successfully when parent is null")
        void shouldCreateRootCategorySuccessfully() {
            CreateCategoryRequest request = new CreateCategoryRequest(
                    "Beverages",
                    "All types of drinks",
                    null
            );

            mockBusinessExists();

            when(categoryRepository.existsInParent(businessId, null, "Beverages", null))
                    .thenReturn(false);

            when(categoryRepository.save(any(Category.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Category result = categoryService.createCategory(tenantId, businessId, request);

            assertNotNull(result);
            assertEquals("Beverages", result.getName());
            assertEquals("All types of drinks", result.getDescription());
            assertNull(result.getParentCategory());
            assertSame(business, result.getBusiness());
            assertTrue(result.isActive());

            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Should create child category successfully when active parent exists")
        void shouldCreateChildCategorySuccessfully() {
            UUID parentId = UUID.randomUUID();
            Category parent = buildCategory(parentId, "Beverages", business, null, true);

            CreateCategoryRequest request = new CreateCategoryRequest(
                    "Hot Drinks",
                    "Tea and coffee",
                    parentId
            );

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(parentId, businessId))
                    .thenReturn(Optional.of(parent));

            when(categoryRepository.existsInParent(businessId, parentId, "Hot Drinks", null))
                    .thenReturn(false);

            when(categoryRepository.save(any(Category.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Category result = categoryService.createCategory(tenantId, businessId, request);

            assertNotNull(result);
            assertEquals("Hot Drinks", result.getName());
            assertEquals("Tea and coffee", result.getDescription());
            assertSame(parent, result.getParentCategory());
            assertSame(business, result.getBusiness());
            assertTrue(result.isActive());

            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Should trim category name before checking duplicate and saving")
        void shouldTrimCategoryName() {
            CreateCategoryRequest request = new CreateCategoryRequest(
                    "   Beverages   ",
                    "Description",
                    null
            );

            mockBusinessExists();

            when(categoryRepository.existsInParent(businessId, null, "Beverages", null))
                    .thenReturn(false);

            when(categoryRepository.save(any(Category.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Category result = categoryService.createCategory(tenantId, businessId, request);

            assertEquals("Beverages", result.getName());
            verify(categoryRepository).existsInParent(businessId, null, "Beverages", null);
        }

        @Test
        @DisplayName("Should throw exception when business is not found")
        void shouldThrowExceptionWhenBusinessNotFound() {
            CreateCategoryRequest request = new CreateCategoryRequest(
                    "Beverages",
                    "Description",
                    null
            );

            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.createCategory(tenantId, businessId, request)
            );

            assertEquals("Business not found", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when business is inactive")
        void shouldThrowExceptionWhenBusinessIsInactive() {
            business.setActive(false);
            mockBusinessExists();

            CreateCategoryRequest request = new CreateCategoryRequest(
                    "Beverages",
                    "Description",
                    null
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.createCategory(tenantId, businessId, request)
            );

            assertEquals("Cannot create categories under an inactive business", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when parent category does not exist")
        void shouldThrowExceptionWhenParentCategoryNotFound() {
            UUID parentId = UUID.randomUUID();
            CreateCategoryRequest request = new CreateCategoryRequest(
                    "Hot Drinks",
                    "Description",
                    parentId
            );

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(parentId, businessId))
                    .thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.createCategory(tenantId, businessId, request)
            );

            assertEquals("Parent category not found", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when parent category is inactive")
        void shouldThrowExceptionWhenParentCategoryIsInactive() {
            UUID parentId = UUID.randomUUID();
            Category parent = buildCategory(parentId, "Beverages", business, null, false);

            CreateCategoryRequest request = new CreateCategoryRequest(
                    "Hot Drinks",
                    "Description",
                    parentId
            );

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(parentId, businessId))
                    .thenReturn(Optional.of(parent));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.createCategory(tenantId, businessId, request)
            );

            assertEquals("Cannot assign a category under an inactive parent category", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when category name already exists under parent")
        void shouldThrowExceptionWhenDuplicateNameExistsUnderParent() {
            CreateCategoryRequest request = new CreateCategoryRequest(
                    "Beverages",
                    "Description",
                    null
            );

            mockBusinessExists();

            when(categoryRepository.existsInParent(businessId, null, "Beverages", null))
                    .thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.createCategory(tenantId, businessId, request)
            );

            assertEquals("A category with this name already exists under this parent", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }
    }

    // =========================================================================
    // GET CATEGORIES (PAGINATED)
    // =========================================================================
    @Nested
    @DisplayName("Get Categories Tests")
    class GetCategoriesTests {

        @Test
        @DisplayName("Should return paginated categories successfully")
        void shouldReturnCategoriesPageSuccessfully() {
            mockBusinessExists();

            Pageable pageable = PageRequest.of(0, 10);
            Category category1 = buildCategory(UUID.randomUUID(), "Beverages", business, null, true);
            Category category2 = buildCategory(UUID.randomUUID(), "Snacks", business, null, true);
            Page<Category> page = new PageImpl<>(List.of(category1, category2), pageable, 2);

            when(categoryRepository.findAllByBusiness_Id(businessId, pageable))
                    .thenReturn(page);

            Page<Category> result = categoryService.getCategories(tenantId, businessId, pageable);

            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals("Beverages", result.getContent().get(0).getName());
            assertEquals("Snacks", result.getContent().get(1).getName());

            verify(categoryRepository).findAllByBusiness_Id(businessId, pageable);
        }

        @Test
        @DisplayName("Should throw exception when business not found during get categories")
        void shouldThrowExceptionWhenBusinessNotFound() {
            Pageable pageable = PageRequest.of(0, 10);

            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.getCategories(tenantId, businessId, pageable)
            );

            assertEquals("Business not found", exception.getMessage());
            verify(categoryRepository, never()).findAllByBusiness_Id(any(), any());
        }
    }

    // =========================================================================
    // GET CATEGORY BY ID
    // =========================================================================
    @Nested
    @DisplayName("Get Category By ID Tests")
    class GetCategoryByIdTests {

        @Test
        @DisplayName("Should return category when found")
        void shouldReturnCategoryByIdSuccessfully() {
            mockBusinessExists();

            Category category = buildCategory(categoryId, "Beverages", business, null, true);

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(category));

            Category result = categoryService.getCategoryById(tenantId, businessId, categoryId);

            assertNotNull(result);
            assertEquals(categoryId, result.getId());
            assertEquals("Beverages", result.getName());

            verify(categoryRepository).findByIdAndBusiness_Id(categoryId, businessId);
        }

        @Test
        @DisplayName("Should throw exception when business not found")
        void shouldThrowExceptionWhenBusinessNotFound() {
            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.getCategoryById(tenantId, businessId, categoryId)
            );

            assertEquals("Business not found", exception.getMessage());
            verify(categoryRepository, never()).findByIdAndBusiness_Id(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.getCategoryById(tenantId, businessId, categoryId)
            );

            assertEquals("Category not found", exception.getMessage());
        }
    }

    // =========================================================================
    // UPDATE CATEGORY
    // =========================================================================
    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category successfully")
        void shouldUpdateCategorySuccessfully() {
            Category existing = buildCategory(categoryId, "Old Name", business, null, true);
            existing.setDescription("Old Description");

            UUID newParentId = UUID.randomUUID();
            Category newParent = buildCategory(newParentId, "Parent Cat", business, null, true);

            UpdateCategoryRequest request = new UpdateCategoryRequest(
                    "Updated Name",
                    "Updated Description",
                    newParentId
            );

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(existing));

            when(categoryRepository.findByIdAndBusiness_Id(newParentId, businessId))
                    .thenReturn(Optional.of(newParent));

            when(categoryRepository.existsInParent(businessId, newParentId, "Updated Name", categoryId))
                    .thenReturn(false);

            when(categoryRepository.save(any(Category.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Category result = categoryService.updateCategory(tenantId, businessId, categoryId, request);

            assertNotNull(result);
            assertEquals("Updated Name", result.getName());
            assertEquals("Updated Description", result.getDescription());
            assertSame(newParent, result.getParentCategory());

            verify(categoryRepository).save(existing);
        }

        @Test
        @DisplayName("Should update category to root level when parentCategoryId is null")
        void shouldUpdateCategoryToRootSuccessfully() {
            UUID oldParentId = UUID.randomUUID();
            Category oldParent = buildCategory(oldParentId, "Old Parent", business, null, true);
            Category existing = buildCategory(categoryId, "Beverages", business, oldParent, true);

            UpdateCategoryRequest request = new UpdateCategoryRequest(
                    "Beverages Updated",
                    "New Desc",
                    null
            );

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(existing));

            when(categoryRepository.existsInParent(businessId, null, "Beverages Updated", categoryId))
                    .thenReturn(false);

            when(categoryRepository.save(any(Category.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Category result = categoryService.updateCategory(tenantId, businessId, categoryId, request);

            assertNotNull(result);
            assertNull(result.getParentCategory());
            assertEquals("Beverages Updated", result.getName());

            verify(categoryRepository).save(existing);
        }

        @Test
        @DisplayName("Should throw exception when business is inactive on update")
        void shouldThrowExceptionWhenBusinessInactive() {
            business.setActive(false);
            mockBusinessExists();

            UpdateCategoryRequest request = new UpdateCategoryRequest("Name", "Desc", null);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategory(tenantId, businessId, categoryId, request)
            );

            assertEquals("Cannot update categories under an inactive business", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when category not found on update")
        void shouldThrowExceptionWhenCategoryNotFound() {
            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.empty());

            UpdateCategoryRequest request = new UpdateCategoryRequest("Name", "Desc", null);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategory(tenantId, businessId, categoryId, request)
            );

            assertEquals("Category not found", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when new parent not found on update")
        void shouldThrowExceptionWhenNewParentNotFound() {
            Category existing = buildCategory(categoryId, "Beverages", business, null, true);
            UUID newParentId = UUID.randomUUID();

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(existing));

            when(categoryRepository.findByIdAndBusiness_Id(newParentId, businessId))
                    .thenReturn(Optional.empty());

            UpdateCategoryRequest request = new UpdateCategoryRequest("Name", "Desc", newParentId);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategory(tenantId, businessId, categoryId, request)
            );

            assertEquals("Parent category not found", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when new parent is inactive on update")
        void shouldThrowExceptionWhenNewParentInactive() {
            Category existing = buildCategory(categoryId, "Beverages", business, null, true);
            UUID newParentId = UUID.randomUUID();
            Category inactiveParent = buildCategory(newParentId, "Parent", business, null, false);

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(existing));

            when(categoryRepository.findByIdAndBusiness_Id(newParentId, businessId))
                    .thenReturn(Optional.of(inactiveParent));

            UpdateCategoryRequest request = new UpdateCategoryRequest("Name", "Desc", newParentId);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategory(tenantId, businessId, categoryId, request)
            );

            assertEquals("Cannot assign a category under an inactive parent category", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when category is set to be its own parent")
        void shouldThrowExceptionWhenCategoryIsItsOwnParent() {
            Category category = buildCategory(categoryId, "Beverages", business, null, true);

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(category));

            UpdateCategoryRequest request = new UpdateCategoryRequest("Beverages", "Desc", categoryId);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategory(tenantId, businessId, categoryId, request)
            );

            assertEquals("A category cannot be its own parent or descendant", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when descendant is assigned as parent (circular hierarchy)")
        void shouldThrowExceptionWhenDescendantAssignedAsParent() {
            Category category = buildCategory(categoryId, "Parent", business, null, true);

            UUID childId = UUID.randomUUID();
            Category child = buildCategory(childId, "Child", business, category, true);

            UUID grandchildId = UUID.randomUUID();
            Category grandchild = buildCategory(grandchildId, "Grandchild", business, child, true);

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(category));

            when(categoryRepository.findByIdAndBusiness_Id(grandchildId, businessId))
                    .thenReturn(Optional.of(grandchild));

            UpdateCategoryRequest request = new UpdateCategoryRequest("Parent", "Desc", grandchildId);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategory(tenantId, businessId, categoryId, request)
            );

            assertEquals("A category cannot be its own parent or descendant", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when loop is detected in parent hierarchy")
        void shouldThrowExceptionWhenLoopDetectedInHierarchy() {
            Category category = buildCategory(categoryId, "Target", business, null, true);

            UUID p1Id = UUID.randomUUID();
            Category p1 = buildCategory(p1Id, "P1", business, null, true);

            UUID p2Id = UUID.randomUUID();
            Category p2 = buildCategory(p2Id, "P2", business, null, true);

            // Create circular loop between p1 and p2: p1 -> p2 -> p1
            p1.setParentCategory(p2);
            p2.setParentCategory(p1);

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(category));

            when(categoryRepository.findByIdAndBusiness_Id(p1Id, businessId))
                    .thenReturn(Optional.of(p1));

            UpdateCategoryRequest request = new UpdateCategoryRequest("Target", "Desc", p1Id);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategory(tenantId, businessId, categoryId, request)
            );

            assertEquals("An existing circular category hierarchy was detected", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when new name already exists under parent")
        void shouldThrowExceptionWhenDuplicateNameExistsUnderParent() {
            Category existing = buildCategory(categoryId, "Old Name", business, null, true);

            UpdateCategoryRequest request = new UpdateCategoryRequest(
                    "Existing Name",
                    "Desc",
                    null
            );

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(existing));

            when(categoryRepository.existsInParent(businessId, null, "Existing Name", categoryId))
                    .thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategory(tenantId, businessId, categoryId, request)
            );

            assertEquals("A category with this name already exists under this parent", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }
    }

    // =========================================================================
    // UPDATE CATEGORY STATUS
    // =========================================================================
    @Nested
    @DisplayName("Update Category Status Tests")
    class UpdateCategoryStatusTests {

        @Test
        @DisplayName("Should deactivate category successfully")
        void shouldDeactivateCategorySuccessfully() {
            Category category = buildCategory(categoryId, "Beverages", business, null, true);

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(category));

            when(categoryRepository.save(any(Category.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Category result = categoryService.updateCategoryStatus(tenantId, businessId, categoryId, false);

            assertNotNull(result);
            assertFalse(result.isActive());

            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should activate category successfully when parent is active")
        void shouldActivateCategorySuccessfullyWhenParentIsActive() {
            UUID parentId = UUID.randomUUID();
            Category parent = buildCategory(parentId, "Parent", business, null, true);
            Category category = buildCategory(categoryId, "Beverages", business, parent, false);

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(category));

            when(categoryRepository.save(any(Category.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Category result = categoryService.updateCategoryStatus(tenantId, businessId, categoryId, true);

            assertNotNull(result);
            assertTrue(result.isActive());

            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should throw exception when activating category under inactive parent")
        void shouldThrowExceptionWhenActivatingUnderInactiveParent() {
            UUID parentId = UUID.randomUUID();
            Category parent = buildCategory(parentId, "Parent", business, null, false);
            Category category = buildCategory(categoryId, "Beverages", business, parent, false);

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(category));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategoryStatus(tenantId, businessId, categoryId, true)
            );

            assertEquals("Cannot assign a category under an inactive parent category", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when business inactive on status update")
        void shouldThrowExceptionWhenBusinessInactive() {
            business.setActive(false);
            mockBusinessExists();

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryService.updateCategoryStatus(tenantId, businessId, categoryId, true)
            );

            assertEquals("Cannot update categories under an inactive business", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }
    }

    // =========================================================================
    // DELETE CATEGORY (SOFT DELETE)
    // =========================================================================
    @Nested
    @DisplayName("Delete Category Tests")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should soft delete category by setting active to false")
        void shouldSoftDeleteCategorySuccessfully() {
            Category category = buildCategory(categoryId, "Beverages", business, null, true);

            mockBusinessExists();

            when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                    .thenReturn(Optional.of(category));

            when(categoryRepository.save(any(Category.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Category result = categoryService.deleteCategory(tenantId, businessId, categoryId);

            assertNotNull(result);
            assertFalse(result.isActive());

            verify(categoryRepository).save(category);
        }
    }
}
