package com.possaas.possaasbackend.productvariant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "product_variant_attribute_values",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_variant_one_value_per_attribute",
                        columnNames = {
                                "product_variant_id",
                                "business_id",
                                "attribute_id"
                        }
                )
        }
)
@Getter
@Setter
public class ProductVariantAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_variant_id", nullable = false)
    private UUID productVariantId;

    @Column(name = "product_attribute_id", nullable = false)
    private UUID productAttributeId;

    @Column(name = "attribute_id", nullable = false)
    private UUID attributeId;

    @Column(name = "attribute_value_id", nullable = false)
    private UUID attributeValueId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
