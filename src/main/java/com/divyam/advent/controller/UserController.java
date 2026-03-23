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

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    public record ParticipantView(Long id, String name, String initials, String culture) {
    }

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
        List<UserResponseDto> responseDtos = List.of(convertToResponseDto(currentUser));
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    @GetMapping("/participants")
    public ResponseEntity<List<ParticipantView>> getParticipants(@AuthenticationPrincipal Jwt jwt) {
        authService.getCurrentUser(jwt);

        List<ParticipantView> participants = userService.getAllUsers().stream()
                .map(user -> new ParticipantView(
                        user.getId(),
                        user.getName(),
                        buildInitials(user.getName()),
                        user.getCountry() != null ? user.getCountry().name() : ""
                ))
                .toList();

        return new ResponseEntity<>(participants, HttpStatus.OK);
    }

    private User convertToUser(UserRequestDto dto) {
        User user = new User(null, dto.getName(), dto.getEmail());
        user.setCountry(dto.getCountry());
        return user;
    }

    private String buildInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }

        String initials = java.util.Arrays.stream(name.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1).toUpperCase())
                .limit(2)
                .reduce("", String::concat);

        return initials.isEmpty() ? "?" : initials;
    }

    private UserResponseDto convertToResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail(), user.getCountry());
    }
}
