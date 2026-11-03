package com.possaas.possaasbackend.branch;


import com.possaas.possaasbackend.business.Business;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "branches",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_branch_business_code",
                        columnNames = {"business_id", "code"}
                ),
                @UniqueConstraint(
                        name = "uq_branch_business_name",
                        columnNames = {"business_id", "name"}
                )
        },
        indexes = {
                @Index(name = "idx_branches_business_id", columnList = "business_id")
        }
)
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Setter
    @Column(nullable = false, length = 150)
    private String name;

    @Setter
    @Column(nullable = false, length = 50)
    private String code;

    @Setter
    @Column(length = 30)
    private String phone;

    @Setter
    @Column(length = 150)
    private String email;

    @Setter
    @Column(length = 255)
    private String address;

    @Setter
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
