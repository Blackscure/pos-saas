
package com.possaas.possaasbackend.category;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.category.dto.CreateCategoryRequest;
import com.possaas.possaasbackend.category.dto.UpdateCategoryRequest;
import com.possaas.possaasbackend.common.exception.GlobalExceptionHandler;
import com.possaas.possaasbackend.security.AuthenticatedUser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private UUID tenantId;
    private UUID businessId;
    private UUID categoryId;
    private Business business;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        business = new Business();
        business.setId(businessId);
        business.setActive(true);

        authenticatedUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "test@example.com",
                businessId,
                tenantId,
                Set.of("MANAGER"),
                Set.of("CATEGORY_CREATE", "CATEGORY_VIEW", "CATEGORY_UPDATE", "CATEGORY_DELETE")
        );

        HandlerMethodArgumentResolver authenticatedUserResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(AuthenticatedUser.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return authenticatedUser;
            }
        };

        mockMvc = MockMvcBuilders
                .standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authenticatedUserResolver)
                .build();
    }

    private Category buildCategory(UUID id, String name, String description, Category parent, boolean active) {
        Category category = new Category();
        ReflectionTestUtils.setField(category, "id", id);
        ReflectionTestUtils.setField(category, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(category, "updatedAt", LocalDateTime.now());
        category.setName(name);
        category.setDescription(description);
        category.setBusiness(business);
        category.setParentCategory(parent);
        category.setActive(active);
        return category;
    }

    // =========================================================================
    // CREATE CATEGORY
    // =========================================================================
    @Nested
    @DisplayName("Create Category Endpoint Tests")
    class CreateCategoryEndpointTests {

        @Test
        @DisplayName("POST /api/businesses/{businessId}/categories - Success")
        void createCategory_Success() throws Exception {
            Category category = buildCategory(categoryId, "Beverages", "All drinks", null, true);

            when(categoryService.createCategory(eq(tenantId), eq(businessId), any(CreateCategoryRequest.class)))
                    .thenReturn(category);

            String json = """
                    {
                        "name": "Beverages",
                        "description": "All drinks",
                        "parentCategoryId": null
                    }
                    """;

            mockMvc.perform(post("/api/businesses/" + businessId + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Category created successfully"))
                    .andExpect(jsonPath("$.data.id").value(categoryId.toString()))
                    .andExpect(jsonPath("$.data.business.id").value(businessId.toString()))
                    .andExpect(jsonPath("$.data.name").value("Beverages"))
                    .andExpect(jsonPath("$.data.description").value("All drinks"))
                    .andExpect(jsonPath("$.data.parentCategory").value(Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.active").value(true));

            verify(categoryService).createCategory(eq(tenantId), eq(businessId), any(CreateCategoryRequest.class));
        }

        @Test
        @DisplayName("POST /api/businesses/{businessId}/categories - With Parent Category - Success")
        void createCategory_WithParentCategory_Success() throws Exception {
            UUID parentId = UUID.randomUUID();
            Category parent = buildCategory(parentId, "Beverages", "Parent", null, true);
            Category child = buildCategory(categoryId, "Hot Drinks", "Child", parent, true);

            when(categoryService.createCategory(eq(tenantId), eq(businessId), any(CreateCategoryRequest.class)))
                    .thenReturn(child);

            String json = """
                    {
                        "name": "Hot Drinks",
                        "description": "Child",
                        "parentCategoryId": "%s"
                    }
                    """.formatted(parentId);

            mockMvc.perform(post("/api/businesses/" + businessId + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(categoryId.toString()))
                    .andExpect(jsonPath("$.data.name").value("Hot Drinks"))
                    .andExpect(jsonPath("$.data.parentCategory.id").value(parentId.toString()))
                    .andExpect(jsonPath("$.data.parentCategory.name").value("Beverages"))
                    .andExpect(jsonPath("$.data.parentCategory.active").value(true));
        }

        @Test
        @DisplayName("POST /api/businesses/{businessId}/categories - Validation Error (Blank Name)")
        void createCategory_ValidationError_BlankName() throws Exception {
            String json = """
                    {
                        "name": "",
                        "description": "Some description"
                    }
                    """;

            mockMvc.perform(post("/api/businesses/" + businessId + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(Matchers.containsString("Category name is required")));

            verify(categoryService, never()).createCategory(any(), any(), any());
        }

        @Test
        @DisplayName("POST /api/businesses/{businessId}/categories - Validation Error (Name Too Long)")
        void createCategory_ValidationError_NameTooLong() throws Exception {
            String longName = "A".repeat(151);
            String json = """
                    {
                        "name": "%s"
                    }
                    """.formatted(longName);

            mockMvc.perform(post("/api/businesses/" + businessId + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(Matchers.containsString("Category name cannot exceed 150 characters")));

            verify(categoryService, never()).createCategory(any(), any(), any());
        }

        @Test
        @DisplayName("POST /api/businesses/{businessId}/categories - Duplicate Name - Returns 400")
        void createCategory_DuplicateName_ReturnsBadRequest() throws Exception {
            when(categoryService.createCategory(eq(tenantId), eq(businessId), any(CreateCategoryRequest.class)))
                    .thenThrow(new IllegalArgumentException("A category with this name already exists under this parent"));

            String json = """
                    {
                        "name": "Beverages"
                    }
                    """;

            mockMvc.perform(post("/api/businesses/" + businessId + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("A category with this name already exists under this parent"))
                    .andExpect(jsonPath("$.data").value(Matchers.nullValue()));
        }

        @Test
        @DisplayName("POST /api/businesses/{businessId}/categories - Inactive Business - Returns 400")
        void createCategory_InactiveBusiness_ReturnsBadRequest() throws Exception {
            when(categoryService.createCategory(eq(tenantId), eq(businessId), any(CreateCategoryRequest.class)))
                    .thenThrow(new IllegalArgumentException("Cannot create categories under an inactive business"));

            String json = """
                    {
                        "name": "Beverages"
                    }
                    """;

            mockMvc.perform(post("/api/businesses/" + businessId + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Cannot create categories under an inactive business"));
        }
    }

    // =========================================================================
    // GET CATEGORIES (PAGINATED)
    // =========================================================================
    @Nested
    @DisplayName("Get Categories Endpoint Tests")
    class GetCategoriesEndpointTests {

        @Test
        @DisplayName("GET /api/businesses/{businessId}/categories - Default Pagination - Success")
        void getCategories_DefaultPagination_Success() throws Exception {
            Category cat1 = buildCategory(UUID.randomUUID(), "Beverages", "Drinks", null, true);
            Category cat2 = buildCategory(UUID.randomUUID(), "Snacks", "Food", null, true);
            Page<Category> page = new PageImpl<>(List.of(cat1, cat2), PageRequest.of(0, 10), 2);

            when(categoryService.getCategories(eq(tenantId), eq(businessId), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/businesses/" + businessId + "/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Categories fetched successfully"))
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.content[0].name").value("Beverages"))
                    .andExpect(jsonPath("$.data.content[1].name").value("Snacks"))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.totalPages").value(1));

            verify(categoryService).getCategories(eq(tenantId), eq(businessId), any(Pageable.class));
        }

        @Test
        @DisplayName("GET /api/businesses/{businessId}/categories - Custom Pagination - Success")
        void getCategories_CustomPagination_Success() throws Exception {
            Page<Category> emptyPage = new PageImpl<>(List.of(), PageRequest.of(2, 5), 0);

            when(categoryService.getCategories(eq(tenantId), eq(businessId), any(Pageable.class)))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/api/businesses/" + businessId + "/categories")
                            .param("page", "2")
                            .param("size", "5")
                            .param("sortBy", "name")
                            .param("sortDir", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.pageNumber").value(2))
                    .andExpect(jsonPath("$.data.pageSize").value(5))
                    .andExpect(jsonPath("$.data.content.length()").value(0));
        }

        @Test
        @DisplayName("GET /api/businesses/{businessId}/categories - Business Not Found - Returns 400")
        void getCategories_BusinessNotFound_ReturnsBadRequest() throws Exception {
            when(categoryService.getCategories(eq(tenantId), eq(businessId), any(Pageable.class)))
                    .thenThrow(new IllegalArgumentException("Business not found"));

            mockMvc.perform(get("/api/businesses/" + businessId + "/categories"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Business not found"));
        }
    }

    // =========================================================================
    // GET CATEGORY BY ID
    // =========================================================================
    @Nested
    @DisplayName("Get Category By ID Endpoint Tests")
    class GetCategoryByIdEndpointTests {

        @Test
        @DisplayName("GET /api/businesses/{businessId}/categories/{categoryId} - Success")
        void getCategoryById_Success() throws Exception {
            Category category = buildCategory(categoryId, "Beverages", "Drinks", null, true);

            when(categoryService.getCategoryById(tenantId, businessId, categoryId))
                    .thenReturn(category);

            mockMvc.perform(get("/api/businesses/" + businessId + "/categories/" + categoryId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Category fetched successfully"))
                    .andExpect(jsonPath("$.data.id").value(categoryId.toString()))
                    .andExpect(jsonPath("$.data.name").value("Beverages"));

            verify(categoryService).getCategoryById(tenantId, businessId, categoryId);
        }

        @Test
        @DisplayName("GET /api/businesses/{businessId}/categories/{categoryId} - Not Found - Returns 400")
        void getCategoryById_NotFound_ReturnsBadRequest() throws Exception {
            when(categoryService.getCategoryById(tenantId, businessId, categoryId))
                    .thenThrow(new IllegalArgumentException("Category not found"));

            mockMvc.perform(get("/api/businesses/" + businessId + "/categories/" + categoryId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Category not found"));
        }
    }

    // =========================================================================
    // UPDATE CATEGORY
    // =========================================================================
    @Nested
    @DisplayName("Update Category Endpoint Tests")
    class UpdateCategoryEndpointTests {

        @Test
        @DisplayName("PUT /api/businesses/{businessId}/categories/{categoryId} - Success")
        void updateCategory_Success() throws Exception {
            Category updated = buildCategory(categoryId, "Soft Drinks", "Cold beverages", null, true);

            when(categoryService.updateCategory(eq(tenantId), eq(businessId), eq(categoryId), any(UpdateCategoryRequest.class)))
                    .thenReturn(updated);

            String json = """
                    {
                        "name": "Soft Drinks",
                        "description": "Cold beverages",
                        "parentCategoryId": null
                    }
                    """;

            mockMvc.perform(put("/api/businesses/" + businessId + "/categories/" + categoryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Category updated successfully"))
                    .andExpect(jsonPath("$.data.id").value(categoryId.toString()))
                    .andExpect(jsonPath("$.data.name").value("Soft Drinks"))
                    .andExpect(jsonPath("$.data.description").value("Cold beverages"));

            verify(categoryService).updateCategory(eq(tenantId), eq(businessId), eq(categoryId), any(UpdateCategoryRequest.class));
        }

        @Test
        @DisplayName("PUT /api/businesses/{businessId}/categories/{categoryId} - Validation Error (Blank Name)")
        void updateCategory_ValidationError_BlankName() throws Exception {
            String json = """
                    {
                        "name": ""
                    }
                    """;

            mockMvc.perform(put("/api/businesses/" + businessId + "/categories/" + categoryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(Matchers.containsString("Category name is required")));

            verify(categoryService, never()).updateCategory(any(), any(), any(), any());
        }

        @Test
        @DisplayName("PUT /api/businesses/{businessId}/categories/{categoryId} - Circular Hierarchy - Returns 400")
        void updateCategory_CircularHierarchy_ReturnsBadRequest() throws Exception {
            when(categoryService.updateCategory(eq(tenantId), eq(businessId), eq(categoryId), any(UpdateCategoryRequest.class)))
                    .thenThrow(new IllegalArgumentException("A category cannot be its own parent or descendant"));

            String json = """
                    {
                        "name": "Beverages",
                        "parentCategoryId": "%s"
                    }
                    """.formatted(categoryId);

            mockMvc.perform(put("/api/businesses/" + businessId + "/categories/" + categoryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("A category cannot be its own parent or descendant"));
        }

        @Test
        @DisplayName("PUT /api/businesses/{businessId}/categories/{categoryId} - Not Found - Returns 400")
        void updateCategory_NotFound_ReturnsBadRequest() throws Exception {
            when(categoryService.updateCategory(eq(tenantId), eq(businessId), eq(categoryId), any(UpdateCategoryRequest.class)))
                    .thenThrow(new IllegalArgumentException("Category not found"));

            String json = """
                    {
                        "name": "Beverages"
                    }
                    """;

            mockMvc.perform(put("/api/businesses/" + businessId + "/categories/" + categoryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Category not found"));
        }
    }

    // =========================================================================
    // UPDATE CATEGORY STATUS
    // =========================================================================
    @Nested
    @DisplayName("Update Category Status Endpoint Tests")
    class UpdateCategoryStatusEndpointTests {

        @Test
        @DisplayName("PATCH /api/businesses/{businessId}/categories/{categoryId}/status?active=true - Activate - Success")
        void updateCategoryStatus_Activate_Success() throws Exception {
            Category category = buildCategory(categoryId, "Beverages", "Drinks", null, true);

            when(categoryService.updateCategoryStatus(tenantId, businessId, categoryId, true))
                    .thenReturn(category);

            mockMvc.perform(patch("/api/businesses/" + businessId + "/categories/" + categoryId + "/status")
                            .param("active", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Category activated successfully"))
                    .andExpect(jsonPath("$.data.id").value(categoryId.toString()))
                    .andExpect(jsonPath("$.data.active").value(true));

            verify(categoryService).updateCategoryStatus(tenantId, businessId, categoryId, true);
        }

        @Test
        @DisplayName("PATCH /api/businesses/{businessId}/categories/{categoryId}/status?active=false - Deactivate - Success")
        void updateCategoryStatus_Deactivate_Success() throws Exception {
            Category category = buildCategory(categoryId, "Beverages", "Drinks", null, false);

            when(categoryService.updateCategoryStatus(tenantId, businessId, categoryId, false))
                    .thenReturn(category);

            mockMvc.perform(patch("/api/businesses/" + businessId + "/categories/" + categoryId + "/status")
                            .param("active", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Category deactivated successfully"))
                    .andExpect(jsonPath("$.data.id").value(categoryId.toString()))
                    .andExpect(jsonPath("$.data.active").value(false));

            verify(categoryService).updateCategoryStatus(tenantId, businessId, categoryId, false);
        }

        @Test
        @DisplayName("PATCH /api/businesses/{businessId}/categories/{categoryId}/status - Inactive Parent - Returns 400")
        void updateCategoryStatus_InactiveParent_ReturnsBadRequest() throws Exception {
            when(categoryService.updateCategoryStatus(tenantId, businessId, categoryId, true))
                    .thenThrow(new IllegalArgumentException("Cannot assign a category under an inactive parent category"));

            mockMvc.perform(patch("/api/businesses/" + businessId + "/categories/" + categoryId + "/status")
                            .param("active", "true"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Cannot assign a category under an inactive parent category"));
        }

        @Test
        @DisplayName("PATCH /api/businesses/{businessId}/categories/{categoryId}/status - Category Not Found - Returns 400")
        void updateCategoryStatus_NotFound_ReturnsBadRequest() throws Exception {
            when(categoryService.updateCategoryStatus(tenantId, businessId, categoryId, true))
                    .thenThrow(new IllegalArgumentException("Category not found"));

            mockMvc.perform(patch("/api/businesses/" + businessId + "/categories/" + categoryId + "/status")
                            .param("active", "true"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Category not found"));
        }
    }

    // =========================================================================
    // DELETE CATEGORY
    // =========================================================================
    @Nested
    @DisplayName("Delete Category Endpoint Tests")
    class DeleteCategoryEndpointTests {

        @Test
        @DisplayName("DELETE /api/businesses/{businessId}/categories/{categoryId} - Success")
        void deleteCategory_Success() throws Exception {
            Category category = buildCategory(categoryId, "Beverages", "Drinks", null, false);

            when(categoryService.deleteCategory(tenantId, businessId, categoryId))
                    .thenReturn(category);

            mockMvc.perform(delete("/api/businesses/" + businessId + "/categories/" + categoryId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Category deleted successfully"))
                    .andExpect(jsonPath("$.data.id").value(categoryId.toString()))
                    .andExpect(jsonPath("$.data.active").value(false));

            verify(categoryService).deleteCategory(tenantId, businessId, categoryId);
        }

        @Test
        @DisplayName("DELETE /api/businesses/{businessId}/categories/{categoryId} - Not Found - Returns 400")
        void deleteCategory_NotFound_ReturnsBadRequest() throws Exception {
            when(categoryService.deleteCategory(tenantId, businessId, categoryId))
                    .thenThrow(new IllegalArgumentException("Category not found"));

            mockMvc.perform(delete("/api/businesses/" + businessId + "/categories/" + categoryId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Category not found"));
        }
    }
}
