package com.divyam.advent.controller;

import com.divyam.advent.dto.UserRequestDto;
import com.divyam.advent.dto.UserResponseDto;
import com.divyam.advent.model.User;
import com.divyam.advent.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        User user = convertToUser(userRequestDto);
        User createdUser = userService.createUser(user);
        UserResponseDto responseDto = convertToResponseDto(createdUser);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserResponseDto responseDto = convertToResponseDto(user);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponseDto> responseDtos = users.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    private User convertToUser(UserRequestDto dto) {
        return new User(null, dto.getName(), dto.getEmail());
    }

    private UserResponseDto convertToResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail());
    }
}
