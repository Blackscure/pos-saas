package com.possaas.possaasbackend.purchaseorder;

import com.possaas.possaasbackend.branch.Branch;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.supplier.Supplier;
import com.possaas.possaasbackend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "purchase_orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_purchase_order_business_number",
                        columnNames = {"business_id", "po_number"}
                )
        },
        indexes = {
                @Index(name = "idx_purchase_orders_business", columnList = "business_id"),
                @Index(name = "idx_purchase_orders_branch", columnList = "branch_id"),
                @Index(name = "idx_purchase_orders_supplier", columnList = "supplier_id"),
                @Index(name = "idx_purchase_orders_status", columnList = "status")
        }
)
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Setter
    @Column(name = "po_number", nullable = false, length = 50)
    private String poNumber;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    @Setter
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Setter
    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Setter
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Setter
    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Setter
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Setter
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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
}
