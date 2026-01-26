package com.divyam.advent.service;

import com.divyam.advent.model.User;
import java.util.List;

public interface UserService {

    User createUser(User user);

    User getUserById(Long id);

    List<User> getAllUsers();
}
