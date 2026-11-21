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
        name = "business_attributes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_business_attributes_id_business",
                        columnNames = {"id", "business_id"}
                ),
                @UniqueConstraint(
                        name = "uq_business_attributes_code",
                        columnNames = {"business_id", "code"}
                )
        }
)
@Getter
@Setter
public class BusinessAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String code;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
