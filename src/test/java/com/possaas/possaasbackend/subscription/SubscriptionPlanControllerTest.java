package com.possaas.possaasbackend.subscription;

import com.possaas.possaasbackend.subscription.dto.SubscriptionPlanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanControllerTest {

    @Mock
    private SubscriptionPlanService subscriptionPlanService;

    private MockMvc mockMvc;

    private UUID planId;
    private SubscriptionPlan plan;

    @BeforeEach
    void setUp() {

        planId = UUID.randomUUID();

        plan = new SubscriptionPlan();
        plan.setId(planId);
        plan.setName("Basic");
        plan.setCode("BASIC");
        plan.setDescription("Basic subscription plan");
        plan.setMonthlyPrice(new BigDecimal("500.00"));
        plan.setYearlyPrice(new BigDecimal("5000.00"));
        plan.setCurrency("KES");
        plan.setMaxBranches(2);
        plan.setMaxUsers(5);
        plan.setActive(true);

        SubscriptionPlanController controller =
                new SubscriptionPlanController(subscriptionPlanService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    // ----------------------------------------------------
    // GET ALL ACTIVE SUBSCRIPTION PLANS
    // ----------------------------------------------------

    @Test
    void getActivePlans_ReturnsPlansSuccessfully() throws Exception {

        when(subscriptionPlanService.getActivePlans())
                .thenReturn(List.of(plan));

        mockMvc.perform(get("/api/public/subscription-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Subscription plans retrieved successfully"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(planId.toString()))
                .andExpect(jsonPath("$.data[0].name").value("Basic"))
                .andExpect(jsonPath("$.data[0].code").value("BASIC"))
                .andExpect(jsonPath("$.data[0].monthlyPrice").value(500.00))
                .andExpect(jsonPath("$.data[0].yearlyPrice").value(5000.00))
                .andExpect(jsonPath("$.data[0].currency").value("KES"))
                .andExpect(jsonPath("$.data[0].maxBranches").value(2))
                .andExpect(jsonPath("$.data[0].maxUsers").value(5));

        verify(subscriptionPlanService).getActivePlans();
    }

    @Test
    void getActivePlans_NoPlans_ReturnsEmptyList() throws Exception {

        when(subscriptionPlanService.getActivePlans())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/public/subscription-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(subscriptionPlanService).getActivePlans();
    }

    // ----------------------------------------------------
    // GET SUBSCRIPTION PLAN BY ID
    // ----------------------------------------------------

    @Test
    void getPlanById_ExistingPlan_ReturnsPlanSuccessfully()
            throws Exception {

        when(subscriptionPlanService.getActivePlanById(planId))
                .thenReturn(plan);

        mockMvc.perform(
                        get("/api/public/subscription-plans/{id}", planId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Subscription plan retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(planId.toString()))
                .andExpect(jsonPath("$.data.name").value("Basic"))
                .andExpect(jsonPath("$.data.code").value("BASIC"))
                .andExpect(jsonPath("$.data.monthlyPrice").value(500.00))
                .andExpect(jsonPath("$.data.yearlyPrice").value(5000.00));

        verify(subscriptionPlanService).getActivePlanById(planId);
    }

    // ----------------------------------------------------
    // GET SUBSCRIPTION PLAN BY CODE
    // ----------------------------------------------------

    @Test
    void getPlanByCode_ExistingPlan_ReturnsPlanSuccessfully()
            throws Exception {

        when(subscriptionPlanService.getActivePlanByCode("BASIC"))
                .thenReturn(plan);

        mockMvc.perform(
                        get("/api/public/subscription-plans/code/{code}", "BASIC")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Subscription plan retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(planId.toString()))
                .andExpect(jsonPath("$.data.name").value("Basic"))
                .andExpect(jsonPath("$.data.code").value("BASIC"))
                .andExpect(jsonPath("$.data.monthlyPrice").value(500.00))
                .andExpect(jsonPath("$.data.yearlyPrice").value(5000.00));

        verify(subscriptionPlanService).getActivePlanByCode("BASIC");
    }
}
