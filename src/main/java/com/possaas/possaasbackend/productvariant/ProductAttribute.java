package com.possaas.possaasbackend.productvariant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "product_attributes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_product_attributes_id_product_business",
                        columnNames = {"id", "product_id", "business_id"}
                ),
                @UniqueConstraint(
                        name = "uq_product_attributes_product_attribute",
                        columnNames = {"product_id", "business_id", "attribute_id"}
                )
        }
)
@Getter
@Setter
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "attribute_id", nullable = false)
    private UUID attributeId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
