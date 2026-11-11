
package com.possaas.possaasbackend.registration;

import com.possaas.possaasbackend.branch.Branch;
import com.possaas.possaasbackend.branch.BranchRepository;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.permission.Permission;
import com.possaas.possaasbackend.permission.PermissionRepository;
import com.possaas.possaasbackend.registration.dto.RegistrationRequest;
import com.possaas.possaasbackend.registration.dto.RegistrationResponse;
import com.possaas.possaasbackend.role.Role;
import com.possaas.possaasbackend.role.RoleRepository;
import com.possaas.possaasbackend.subscription.Subscription;
import com.possaas.possaasbackend.subscription.SubscriptionService;
import com.possaas.possaasbackend.subscription.SubscriptionStatus;
import com.possaas.possaasbackend.tenant.Tenant;
import com.possaas.possaasbackend.tenant.TenantRepository;
import com.possaas.possaasbackend.user.User;
import com.possaas.possaasbackend.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class PublicRegistrationService {

    private static final int TRIAL_DURATION_DAYS = 14;

    private final TenantRepository tenantRepository;
    private final BusinessRepository businessRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;

    public PublicRegistrationService(
            TenantRepository tenantRepository,
            BusinessRepository businessRepository,
            BranchRepository branchRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder,
            SubscriptionService subscriptionService
    ) {
        this.tenantRepository = tenantRepository;
        this.businessRepository = businessRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionService = subscriptionService;
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {

        // 1. Normalize and validate tenant details.
        String tenantCode = request.tenant().code()
                .trim()
                .toUpperCase(Locale.ROOT);

        String tenantEmail = normalizeEmail(request.tenant().email());

        if (tenantRepository.existsByCode(tenantCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tenant code is already registered"
            );
        }

        if (tenantRepository.existsByEmail(tenantEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tenant email is already registered"
            );
        }

        // 2. Create tenant.
        Tenant tenant = new Tenant();
        tenant.setName(request.tenant().name().trim());
        tenant.setCode(tenantCode);
        tenant.setEmail(tenantEmail);
        tenant.setActive(true);

        tenant = tenantRepository.save(tenant);

        // 3. Create business.
        Business business = new Business();
        business.setTenant(tenant);
        business.setName(request.business().name().trim());
        business.setBusinessType(request.business().businessType());
        business.setRegistrationNumber(
                request.business().registrationNumber()
        );
        business.setPhone(request.business().phone());
        business.setEmail(
                normalizeOptionalEmail(request.business().email())
        );
        business.setAddress(request.business().address());
        business.setCurrency(
                request.business().currency() == null ||
                        request.business().currency().isBlank()
                        ? "KES"
                        : request.business().currency()
                        .trim()
                        .toUpperCase(Locale.ROOT)
        );
        business.setTaxEnabled(
                request.business().taxEnabled() == null ||
                        request.business().taxEnabled()
        );
        business.setActive(true);

        business = businessRepository.save(business);

        // 4. Create the default branch.
        Branch branch = new Branch();
        branch.setBusiness(business);
        branch.setName(request.branch().name().trim());
        branch.setCode(
                request.branch().code()
                        .trim()
                        .toUpperCase(Locale.ROOT)
        );
        branch.setPhone(request.branch().phone());
        branch.setEmail(
                normalizeOptionalEmail(request.branch().email())
        );
        branch.setAddress(request.branch().address());
        branch.setActive(true);

        branch = branchRepository.save(branch);

        // 5. Create the OWNER role with all configured permissions.
        List<Permission> permissions = permissionRepository.findAll();

        if (permissions.isEmpty()) {
            throw new IllegalStateException(
                    "No permissions are configured. Cannot create an owner account."
            );
        }

        Role ownerRole = new Role();
        ownerRole.setBusiness(business);
        ownerRole.setName("OWNER");
        ownerRole.setDescription("Business owner with full access");
        ownerRole.setActive(true);
        ownerRole.setPermissions(new HashSet<>(permissions));

        ownerRole = roleRepository.save(ownerRole);

        // 6. Create the first user.
        User owner = new User();
        owner.setBusiness(business);
        owner.setFirstName(request.owner().firstName().trim());
        owner.setLastName(request.owner().lastName().trim());
        owner.setEmail(normalizeEmail(request.owner().email()));
        owner.setPhone(request.owner().phone());
        owner.setPassword(
                passwordEncoder.encode(request.owner().password())
        );
        owner.setActive(true);

        Set<Role> ownerRoles = new HashSet<>();
        ownerRoles.add(ownerRole);
        owner.setRoles(ownerRoles);

        owner = userRepository.save(owner);

        // 7. Automatically create a 14-day trial subscription.
        LocalDateTime trialStartDate = LocalDateTime.now();
        LocalDateTime trialEndDate =
                trialStartDate.plusDays(TRIAL_DURATION_DAYS);

        Subscription subscription =
                subscriptionService.createSubscription(
                        tenant.getId(),
                        request.subscriptionPlanId(),
                        SubscriptionStatus.TRIALING,
                        trialStartDate,
                        null,
                        trialEndDate
                );

        // 8. Return registration and subscription details.
        return new RegistrationResponse(
                tenant.getId(),
                business.getId(),
                branch.getId(),
                owner.getId(),
                owner.getEmail(),
                subscription.getId(),
                subscription.getPlan().getId(),
                subscription.getPlan().getName(),
                subscription.getStatus().name(),
                subscription.getTrialEndDate(),
                "Business registered successfully. Your 14-day free trial has started. You can now log in."
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptionalEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return normalizeEmail(email);
    }
}
