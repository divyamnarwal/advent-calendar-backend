package com.divyam.advent.service;

import com.divyam.advent.enums.Culture;
import com.divyam.advent.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(User user);

    User getUserById(Long id);

    List<User> getAllUsers();

    Optional<User> getByEmail(String email);

    Optional<User> getByAuthSubject(String authProvider, String authSubject);

    User upsertAuthUser(
            String authProvider,
            String authSubject,
            String email,
            String name,
            Culture country
    );

    User setAdminRole(Long userId, boolean admin);

    User setBan(Long userId, String reason, java.time.LocalDateTime expiresAt);

    User clearBan(Long userId);

    /**
     * Returns the user with ban state reconciled: if the ban has expired by
     * wall-clock time, the fields are cleared and persisted. After the call,
     * {@code user.isCurrentlyBanned()} is the authoritative ban state.
     */
    User reconcileBan(User user);
}
