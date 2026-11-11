
package com.possaas.possaasbackend.subscription;

import com.possaas.possaasbackend.tenant.Tenant;
import com.possaas.possaasbackend.tenant.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private UUID tenantId;
    private UUID planId;
    private UUID subscriptionId;

    private Tenant tenant;
    private SubscriptionPlan plan;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        planId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();

        tenant = mock(Tenant.class);
        plan = mock(SubscriptionPlan.class);
    }

    private void stubActiveTenant() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(tenant.isActive()).thenReturn(true);
    }

    private void stubActivePlan() {
        when(subscriptionPlanRepository.findById(planId))
                .thenReturn(Optional.of(plan));

        when(plan.isActive()).thenReturn(true);
    }

    @Test
    void createSubscription_ValidActiveSubscription_Success() {

        stubActiveTenant();
        stubActivePlan();

        when(subscriptionRepository.existsByTenant_IdAndStatusIn(
                eq(tenantId), anyList()
        )).thenReturn(false);

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(1);

        Subscription result = subscriptionService.createSubscription(
                tenantId,
                planId,
                SubscriptionStatus.ACTIVE,
                startDate,
                endDate,
                null
        );

        assertNotNull(result);
        assertSame(tenant, result.getTenant());
        assertSame(plan, result.getPlan());
        assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void createSubscription_ValidTrialSubscription_Success() {

        stubActiveTenant();
        stubActivePlan();

        when(subscriptionRepository.existsByTenant_IdAndStatusIn(
                eq(tenantId), anyList()
        )).thenReturn(false);

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime trialEndDate = startDate.plusDays(14);

        Subscription result = subscriptionService.createSubscription(
                tenantId,
                planId,
                SubscriptionStatus.TRIALING,
                startDate,
                null,
                trialEndDate
        );

        assertNotNull(result);
        assertEquals(SubscriptionStatus.TRIALING, result.getStatus());
        assertEquals(trialEndDate, result.getTrialEndDate());

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void createSubscription_InactiveTenant_ThrowsException() {

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(tenant.isActive()).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.createSubscription(
                        tenantId,
                        planId,
                        SubscriptionStatus.ACTIVE,
                        null,
                        null,
                        null
                )
        );

        assertEquals(
                "Cannot create a subscription for an inactive tenant",
                exception.getMessage()
        );

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createSubscription_TenantNotFound_ThrowsException() {

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.createSubscription(
                        tenantId,
                        planId,
                        SubscriptionStatus.ACTIVE,
                        null,
                        null,
                        null
                )
        );

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createSubscription_PlanNotFound_ThrowsException() {

        stubActiveTenant();

        when(subscriptionPlanRepository.findById(planId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.createSubscription(
                        tenantId,
                        planId,
                        SubscriptionStatus.ACTIVE,
                        null,
                        null,
                        null
                )
        );

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createSubscription_InactivePlan_ThrowsException() {

        stubActiveTenant();

        when(subscriptionPlanRepository.findById(planId))
                .thenReturn(Optional.of(plan));

        when(plan.isActive()).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.createSubscription(
                        tenantId,
                        planId,
                        SubscriptionStatus.ACTIVE,
                        null,
                        null,
                        null
                )
        );

        assertEquals(
                "Cannot subscribe to an inactive plan",
                exception.getMessage()
        );

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createSubscription_ExistingCurrentSubscription_ThrowsException() {

        stubActiveTenant();
        stubActivePlan();

        when(subscriptionRepository.existsByTenant_IdAndStatusIn(
                eq(tenantId), anyList()
        )).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.createSubscription(
                        tenantId,
                        planId,
                        SubscriptionStatus.ACTIVE,
                        null,
                        null,
                        null
                )
        );

        assertEquals(
                "Tenant already has a current subscription",
                exception.getMessage()
        );

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createSubscription_InvalidStatus_ThrowsException() {

        stubActiveTenant();
        stubActivePlan();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.createSubscription(
                        tenantId,
                        planId,
                        SubscriptionStatus.SUSPENDED,
                        null,
                        null,
                        null
                )
        );

        assertEquals(
                "New subscriptions must be TRIALING or ACTIVE",
                exception.getMessage()
        );

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createSubscription_TrialWithoutEndDate_ThrowsException() {

        stubActiveTenant();
        stubActivePlan();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.createSubscription(
                        tenantId,
                        planId,
                        SubscriptionStatus.TRIALING,
                        null,
                        null,
                        null
                )
        );

        assertEquals(
                "Trial end date is required for trial subscriptions",
                exception.getMessage()
        );

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createSubscription_EndDateBeforeStartDate_ThrowsException() {

        stubActiveTenant();
        stubActivePlan();

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.minusDays(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.createSubscription(
                        tenantId,
                        planId,
                        SubscriptionStatus.ACTIVE,
                        startDate,
                        endDate,
                        null
                )
        );

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void getCurrentSubscription_ExistingSubscription_ReturnsSubscription() {

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        Subscription subscription = new Subscription();
        subscription.setTenant(tenant);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        when(subscriptionRepository
                .findFirstByTenant_IdAndStatusInOrderByCreatedAtDesc(
                        eq(tenantId), anyList()
                ))
                .thenReturn(Optional.of(subscription));

        Subscription result =
                subscriptionService.getCurrentSubscription(tenantId);

        assertNotNull(result);
        assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getCurrentSubscription_NoSubscription_ThrowsException() {

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(subscriptionRepository
                .findFirstByTenant_IdAndStatusInOrderByCreatedAtDesc(
                        eq(tenantId), anyList()
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.getCurrentSubscription(tenantId)
        );
    }

    @Test
    void getSubscriptionHistory_ReturnsSubscriptionPage() {

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        Page<Subscription> expectedPage =
                new PageImpl<>(List.of(new Subscription()));

        when(subscriptionRepository.findAllByTenant_Id(
                eq(tenantId), any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<Subscription> result =
                subscriptionService.getSubscriptionHistory(
                        tenantId,
                        Pageable.unpaged()
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void cancelSubscription_ValidSubscription_Success() {

        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        when(subscriptionRepository.findByIdAndTenant_Id(
                subscriptionId, tenantId
        )).thenReturn(Optional.of(subscription));

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Subscription result = subscriptionService.cancelSubscription(
                tenantId,
                subscriptionId
        );

        assertEquals(SubscriptionStatus.CANCELLED, result.getStatus());
        assertNotNull(result.getCancelledAt());

        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void cancelSubscription_SubscriptionBelongsToAnotherTenant_ThrowsException() {

        when(subscriptionRepository.findByIdAndTenant_Id(
                subscriptionId, tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.cancelSubscription(
                        tenantId,
                        subscriptionId
                )
        );

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void cancelSubscription_AlreadyCancelled_ThrowsException() {

        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.CANCELLED);

        when(subscriptionRepository.findByIdAndTenant_Id(
                subscriptionId, tenantId
        )).thenReturn(Optional.of(subscription));

        assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.cancelSubscription(
                        tenantId,
                        subscriptionId
                )
        );

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void cancelSubscription_AlreadyExpired_ThrowsException() {

        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.EXPIRED);

        when(subscriptionRepository.findByIdAndTenant_Id(
                subscriptionId, tenantId
        )).thenReturn(Optional.of(subscription));

        assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.cancelSubscription(
                        tenantId,
                        subscriptionId
                )
        );

        verify(subscriptionRepository, never()).save(any());
    }
}
