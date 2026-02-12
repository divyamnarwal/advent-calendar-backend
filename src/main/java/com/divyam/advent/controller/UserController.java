package com.divyam.advent.controller;

import com.divyam.advent.dto.UserRequestDto;
import com.divyam.advent.dto.UserResponseDto;
import com.divyam.advent.model.User;
import com.divyam.advent.service.AuthService;
import com.divyam.advent.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        User user = convertToUser(userRequestDto);
        User createdUser = userService.createUser(user);
        UserResponseDto responseDto = convertToResponseDto(createdUser);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        authService.validateUserAccess(jwt, id);
        User user = userService.getUserById(id);
        UserResponseDto responseDto = convertToResponseDto(user);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(@AuthenticationPrincipal Jwt jwt) {
        User currentUser = authService.getCurrentUser(jwt);
        List<User> users = Collections.singletonList(currentUser);
        List<UserResponseDto> responseDtos = users.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    private User convertToUser(UserRequestDto dto) {
        User user = new User(null, dto.getName(), dto.getEmail());
        user.setCountry(dto.getCountry());
        return user;
    }

    private UserResponseDto convertToResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail(), user.getCountry());
    }
}
