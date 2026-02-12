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
}
