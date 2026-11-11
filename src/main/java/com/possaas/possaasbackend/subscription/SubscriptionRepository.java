package com.possaas.possaasbackend.subscription;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, UUID> {

    Page<Subscription> findAllByTenant_Id(
            UUID tenantId,
            Pageable pageable
    );

    Optional<Subscription> findByIdAndTenant_Id(
            UUID subscriptionId,
            UUID tenantId
    );

    Optional<Subscription> findFirstByTenant_IdAndStatusInOrderByCreatedAtDesc(
            UUID tenantId,
            Iterable<SubscriptionStatus> statuses
    );

    boolean existsByTenant_IdAndStatusIn(
            UUID tenantId,
            Iterable<SubscriptionStatus> statuses
    );
}
