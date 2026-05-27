package com.divyam.advent.controller;

import com.divyam.advent.dto.AuthEnsureUserRequest;
import com.divyam.advent.dto.UserResponseDto;
import com.divyam.advent.model.User;
import com.divyam.advent.security.AdminGuard;
import com.divyam.advent.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AdminGuard adminGuard;

    public AuthController(AuthService authService, AdminGuard adminGuard) {
        this.authService = authService;
        this.adminGuard = adminGuard;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        User user = authService.getCurrentUser(jwt);
        return ResponseEntity.ok(toResponse(user, jwt));
    }

    @PostMapping("/ensure-user")
    public ResponseEntity<UserResponseDto> ensureUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AuthEnsureUserRequest request
    ) {
        User user = authService.ensureUser(jwt, request);
        return ResponseEntity.ok(toResponse(user, jwt));
    }

    private UserResponseDto toResponse(User user, Jwt jwt) {
        String subject = jwt != null && jwt.getSubject() != null ? jwt.getSubject().trim() : null;
        boolean superAdmin = adminGuard.isSuperAdmin(subject);
        boolean admin = superAdmin || user.isAdmin();
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCountry(),
                admin,
                superAdmin
        );
    }
}
