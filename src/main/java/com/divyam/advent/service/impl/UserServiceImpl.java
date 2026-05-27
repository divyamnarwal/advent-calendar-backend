package com.divyam.advent.service.impl;

import com.divyam.advent.enums.Culture;
import com.divyam.advent.enums.ThemePreference;
import com.divyam.advent.exception.ResourceNotFoundException;
import com.divyam.advent.model.User;
import com.divyam.advent.repository.UserRepository;
import com.divyam.advent.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public User setAdminRole(Long userId, boolean admin) {
        User user = getUserById(userId);
        user.setAdmin(admin);
        return userRepository.save(user);
    }

    @Override
    public User setBan(Long userId, String reason, java.time.LocalDateTime expiresAt) {
        User user = getUserById(userId);
        user.setBanned(true);
        user.setBanReason(reason == null ? null : reason.trim().isEmpty() ? null : reason.trim());
        user.setBanExpiresAt(expiresAt);
        return userRepository.save(user);
    }

    @Override
    public User clearBan(Long userId) {
        User user = getUserById(userId);
        user.setBanned(false);
        user.setBanReason(null);
        user.setBanExpiresAt(null);
        return userRepository.save(user);
    }

    @Override
    public User reconcileBan(User user) {
        if (user == null || !user.isBanned()) {
            return user;
        }
        java.time.LocalDateTime expiresAt = user.getBanExpiresAt();
        if (expiresAt != null && !expiresAt.isAfter(java.time.LocalDateTime.now())) {
            user.setBanned(false);
            user.setBanReason(null);
            user.setBanExpiresAt(null);
            return userRepository.save(user);
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    @Override
    public Optional<User> getByAuthSubject(String authProvider, String authSubject) {
        if (authProvider == null || authProvider.trim().isEmpty()
                || authSubject == null || authSubject.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByAuthProviderAndAuthSubject(
                authProvider.trim(),
                authSubject.trim()
        );
    }

    @Override
    public User upsertAuthUser(
            String authProvider,
            String authSubject,
            String email,
            String name,
            Culture country
    ) {
        if (authProvider == null || authProvider.trim().isEmpty()) {
            throw new IllegalArgumentException("authProvider is required");
        }
        if (authSubject == null || authSubject.trim().isEmpty()) {
            throw new IllegalArgumentException("authSubject is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email is required");
        }

        String normalizedProvider = authProvider.trim();
        String normalizedSubject = authSubject.trim();
        String normalizedEmail = email.trim().toLowerCase();
        String resolvedName = (name == null || name.trim().isEmpty()) ? "Advent User" : name.trim();
        Culture resolvedCountry = country != null ? country : Culture.GLOBAL;

        Optional<User> existingBySubject = userRepository.findByAuthProviderAndAuthSubject(
                normalizedProvider, normalizedSubject
        );
        if (existingBySubject.isPresent()) {
            User user = existingBySubject.get();
            boolean changed = false;

            if (user.getName() == null || !user.getName().equals(resolvedName)) {
                user.setName(resolvedName);
                changed = true;
            }
            if (user.getCountry() == null) {
                user.setCountry(resolvedCountry);
                changed = true;
            }
            if (user.getStreak() == null) {
                user.setStreak(0);
                changed = true;
            }
            if (user.getTotalPoints() == null) {
                user.setTotalPoints(0L);
                changed = true;
            }
            if (user.getBadges() == null) {
                user.setBadges(new java.util.LinkedHashSet<>());
                changed = true;
            }
            if (user.getThemePreference() == null) {
                user.setThemePreference(ThemePreference.SYSTEM);
                changed = true;
            }
            String currentEmail = user.getEmail();
            if (currentEmail == null || !currentEmail.equalsIgnoreCase(normalizedEmail)) {
                Optional<User> existingByEmailForUpdate = userRepository.findByEmailIgnoreCase(normalizedEmail);
                if (existingByEmailForUpdate.isPresent()
                        && !existingByEmailForUpdate.get().getId().equals(user.getId())) {
                    throw new IllegalStateException("Email is already linked to another account");
                }
                user.setEmail(normalizedEmail);
                changed = true;
            }

            try {
                return changed ? userRepository.save(user) : user;
            } catch (DataAccessException e) {
                log.error("Database error while updating user", e);
                throw new IllegalStateException("Failed to update user: " + e.getMessage(), e);
            }
        }

        Optional<User> existingByEmail = userRepository.findByEmailIgnoreCase(normalizedEmail);
        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();
            user.setAuthProvider(normalizedProvider);
            user.setAuthSubject(normalizedSubject);

            if (user.getName() == null || user.getName().trim().isEmpty()) {
                user.setName(resolvedName);
            }
            if (user.getCountry() == null) {
                user.setCountry(resolvedCountry);
            }
            if (user.getStreak() == null) {
                user.setStreak(0);
            }
            if (user.getTotalPoints() == null) {
                user.setTotalPoints(0L);
            }
            if (user.getBadges() == null) {
                user.setBadges(new java.util.LinkedHashSet<>());
            }
            if (user.getThemePreference() == null) {
                user.setThemePreference(ThemePreference.SYSTEM);
            }

            try {
                return userRepository.save(user);
            } catch (DataAccessException e) {
                log.error("Database error while linking auth to existing user", e);
                throw new IllegalStateException("Failed to link auth to user: " + e.getMessage(), e);
            }
        }

        User user = new User();
        user.setName(resolvedName);
        user.setEmail(normalizedEmail);
        user.setCountry(resolvedCountry);
        user.setAuthProvider(normalizedProvider);
        user.setAuthSubject(normalizedSubject);
        user.setStreak(0);
        user.setTotalPoints(0L);
        user.setBadges(new java.util.LinkedHashSet<>());
        user.setThemePreference(ThemePreference.SYSTEM);
        try {
            return userRepository.save(user);
        } catch (DataAccessException e) {
            log.error("Database error while creating new user", e);
            throw new IllegalStateException("Failed to create user: " + e.getMessage(), e);
        }
    }
}
