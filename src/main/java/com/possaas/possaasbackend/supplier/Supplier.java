package com.possaas.possaasbackend.supplier;

import com.possaas.possaasbackend.business.Business;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Setter
    @Column(nullable = false, length = 150)
    private String name;

    @Setter
    @Column(length = 150)
    private String contactPerson;

    @Setter
    @Column(length = 30)
    private String phone;

    @Setter
    @Column(length = 150)
    private String email;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String address;

    @Setter
    @Column(length = 50)
    private String taxPin;

    @Setter
    @Column(length = 100)
    private String paymentTerms;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Setter
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
}
