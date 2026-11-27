package com.possaas.possaasbackend.purchaseorder;

import com.possaas.possaasbackend.product.Product;
import com.possaas.possaasbackend.productvariant.ProductVariant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "purchase_order_items",
        indexes = {
                @Index(name = "idx_purchase_order_items_purchase_order", columnList = "purchase_order_id"),
                @Index(name = "idx_purchase_order_items_product", columnList = "product_id"),
                @Index(name = "idx_purchase_order_items_variant", columnList = "product_variant_id")
        }
)
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Setter
    @Column(nullable = false)
    private Integer quantityOrdered;

    @Setter
    @Column(nullable = false)
    private Integer quantityReceived = 0;

    @Setter
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Setter
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Setter
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Setter
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Setter
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
