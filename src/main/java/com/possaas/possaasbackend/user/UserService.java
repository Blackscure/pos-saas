package com.possaas.possaasbackend.user;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.user.dto.CreateUserRequest;
import com.possaas.possaasbackend.user.dto.UpdateUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            BusinessRepository businessRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Create a user
    public User createUser(UUID businessId, CreateUserRequest request) {

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new RuntimeException("Business not found"));

        if (!business.isActive()) {
            throw new RuntimeException("Cannot create users under an inactive business");
        }

        String email = normalizeEmail(request.email());

        if (userRepository.existsByBusiness_IdAndEmail(businessId, email)) {
            throw new RuntimeException("Email already exists in this business");
        }

        User user = new User();

        user.setBusiness(business);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPhone(request.phone());

        // Never save a plain-text password
        user.setPassword(passwordEncoder.encode(request.password()));

        user.setActive(true);

        return userRepository.save(user);
    }

    // Get all users belonging to a business
    @Transactional(readOnly = true)
    public Page<User> getUsersByBusiness(UUID businessId, Pageable pageable) {

        if (!businessRepository.existsById(businessId)) {
            throw new RuntimeException("Business not found");
        }

        return userRepository.findAllByBusiness_Id(businessId, pageable);
    }

    // Get one user belonging to a business
    @Transactional(readOnly = true)
    public User getUserById(UUID businessId, UUID userId) {

        return userRepository.findByIdAndBusiness_Id(userId, businessId)
                .orElseThrow(() ->
                        new RuntimeException("User not found in this business"));
    }

    // Update user information
    public User updateUser(
            UUID businessId,
            UUID userId,
            UpdateUserRequest request
    ) {

        User user = getUserById(businessId, userId);

        String email = normalizeEmail(request.email());

        boolean emailExists = userRepository
                .existsByBusiness_IdAndEmail(businessId, email);

        if (emailExists && !user.getEmail().equals(email)) {
            throw new RuntimeException("Email already exists in this business");
        }

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPhone(request.phone());

        return userRepository.save(user);
    }

    // Activate or deactivate a user
    public User updateUserStatus(
            UUID businessId,
            UUID userId,
            boolean active
    ) {

        User user = getUserById(businessId, userId);

        user.setActive(active);

        return userRepository.save(user);
    }

    // Soft-delete a user
    public void deleteUser(UUID businessId, UUID userId) {

        User user = getUserById(businessId, userId);

        user.setActive(false);

        userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}