package com.possaas.possaasbackend.subscription;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "subscription_plans",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "subscription_plans_code_key",
                        columnNames = "code"
                )
        }
)
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 255)
    private String description;

    @Column(
            name = "monthly_price",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal monthlyPrice = BigDecimal.ZERO;

    @Column(
            name = "yearly_price",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal yearlyPrice = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "KES";

    @Column(name = "max_branches")
    private Integer maxBranches;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public SubscriptionPlan() {
    }
}
