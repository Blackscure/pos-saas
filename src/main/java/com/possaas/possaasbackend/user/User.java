package com.possaas.possaasbackend.user;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.role.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_business_email",
                        columnNames = {"business_id", "email"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_users_business_id",
                        columnList = "business_id"
                )
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Setter
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Setter
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Setter
    @Column(nullable = false, length = 150)
    private String email;

    @Setter
    @Column(length = 30)
    private String phone;

    @Setter
    @Column(nullable = false)
    private String password;

    @Setter
    @Column(nullable = false)
    private boolean active = true;

    @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

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
