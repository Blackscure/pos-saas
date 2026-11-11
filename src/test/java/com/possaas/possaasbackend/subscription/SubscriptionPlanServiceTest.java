package com.possaas.possaasbackend.subscription;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanServiceTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @InjectMocks
    private SubscriptionPlanService subscriptionPlanService;

    private UUID planId;
    private SubscriptionPlan plan;

    @BeforeEach
    void setUp() {
        planId = UUID.randomUUID();
        plan = new SubscriptionPlan();
        plan.setId(planId);
        plan.setName("Basic");
        plan.setCode("BASIC");
        plan.setActive(true);
    }

    // ----------------------------------------------------
    // GET ACTIVE PLANS
    // ----------------------------------------------------

    @Test
    void getActivePlans_ReturnsActivePlans() {

        List<SubscriptionPlan> plans = List.of(plan);

        when(subscriptionPlanRepository.findAllByActiveTrue())
                .thenReturn(plans);

        List<SubscriptionPlan> result =
                subscriptionPlanService.getActivePlans();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BASIC", result.get(0).getCode());

        verify(subscriptionPlanRepository).findAllByActiveTrue();
    }

    @Test
    void getActivePlans_NoActivePlans_ReturnsEmptyList() {

        when(subscriptionPlanRepository.findAllByActiveTrue())
                .thenReturn(List.of());

        List<SubscriptionPlan> result =
                subscriptionPlanService.getActivePlans();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(subscriptionPlanRepository).findAllByActiveTrue();
    }

    // ----------------------------------------------------
    // GET ACTIVE PLAN BY ID
    // ----------------------------------------------------

    @Test
    void getActivePlanById_ExistingActivePlan_ReturnsPlan() {

        when(subscriptionPlanRepository.findById(planId))
                .thenReturn(Optional.of(plan));

        SubscriptionPlan result =
                subscriptionPlanService.getActivePlanById(planId);

        assertNotNull(result);
        assertEquals(planId, result.getId());
        assertEquals("BASIC", result.getCode());

        verify(subscriptionPlanRepository).findById(planId);
    }

    @Test
    void getActivePlanById_PlanNotFound_ThrowsException() {

        when(subscriptionPlanRepository.findById(planId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionPlanService.getActivePlanById(planId)
        );

        assertEquals("Subscription plan not found", exception.getMessage());

        verify(subscriptionPlanRepository).findById(planId);
    }

    @Test
    void getActivePlanById_InactivePlan_ThrowsException() {

        plan.setActive(false);

        when(subscriptionPlanRepository.findById(planId))
                .thenReturn(Optional.of(plan));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionPlanService.getActivePlanById(planId)
        );

        assertEquals("Subscription plan is inactive", exception.getMessage());

        verify(subscriptionPlanRepository).findById(planId);
    }

    // ----------------------------------------------------
    // GET ACTIVE PLAN BY CODE
    // ----------------------------------------------------

    @Test
    void getActivePlanByCode_ExistingActivePlan_ReturnsPlan() {

        when(subscriptionPlanRepository.findByCode("BASIC"))
                .thenReturn(Optional.of(plan));

        SubscriptionPlan result =
                subscriptionPlanService.getActivePlanByCode("BASIC");

        assertNotNull(result);
        assertEquals("BASIC", result.getCode());
        assertEquals("Basic", result.getName());

        verify(subscriptionPlanRepository).findByCode("BASIC");
    }

    @Test
    void getActivePlanByCode_PlanNotFound_ThrowsException() {

        when(subscriptionPlanRepository.findByCode("UNKNOWN"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionPlanService.getActivePlanByCode("UNKNOWN")
        );

        assertEquals("Subscription plan not found", exception.getMessage());

        verify(subscriptionPlanRepository).findByCode("UNKNOWN");
    }

    @Test
    void getActivePlanByCode_InactivePlan_ThrowsException() {

        plan.setActive(false);

        when(subscriptionPlanRepository.findByCode("BASIC"))
                .thenReturn(Optional.of(plan));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionPlanService.getActivePlanByCode("BASIC")
        );

        assertEquals("Subscription plan is inactive", exception.getMessage());

        verify(subscriptionPlanRepository).findByCode("BASIC");
    }
}
