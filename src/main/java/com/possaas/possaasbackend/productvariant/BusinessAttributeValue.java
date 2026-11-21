package com.possaas.possaasbackend.productvariant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "business_attribute_values",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_attribute_values_id_business_attribute",
                        columnNames = {"id", "business_id", "attribute_id"}
                ),
                @UniqueConstraint(
                        name = "uq_attribute_values_value",
                        columnNames = {"business_id", "attribute_id", "value"}
                )
        }
)
@Getter
@Setter
public class BusinessAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "attribute_id", nullable = false)
    private UUID attributeId;

    @Column(nullable = false, length = 100)
    private String value;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
