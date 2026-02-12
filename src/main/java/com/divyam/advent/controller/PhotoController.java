package com.divyam.advent.controller;

import com.divyam.advent.dto.PhotoCreateRequest;
import com.divyam.advent.dto.PhotoResponseDto;
import com.divyam.advent.dto.PhotoUploadSignatureResponse;
import com.divyam.advent.model.User;
import com.divyam.advent.service.AuthService;
import com.divyam.advent.service.PhotoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;
    private final AuthService authService;

    public PhotoController(PhotoService photoService, AuthService authService) {
        this.photoService = photoService;
        this.authService = authService;
    }

    @GetMapping("/upload-signature")
    public ResponseEntity<PhotoUploadSignatureResponse> getUploadSignature(
            @AuthenticationPrincipal Jwt jwt
    ) {
        User currentUser = authService.getCurrentUser(jwt);
        return ResponseEntity.ok(photoService.getUploadSignature(currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<PhotoResponseDto> createPhoto(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PhotoCreateRequest request
    ) {
        User currentUser = authService.getCurrentUser(jwt);
        PhotoResponseDto created = photoService.createPhoto(currentUser.getId(), request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PhotoResponseDto>> getPhotos(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String month
    ) {
        User currentUser = authService.getCurrentUser(jwt);
        YearMonth parsedMonth = null;
        if (month != null && !month.trim().isEmpty()) {
            parsedMonth = YearMonth.parse(month.trim());
        }

        List<PhotoResponseDto> photos = photoService.getPhotos(currentUser.getId(), parsedMonth);
        return ResponseEntity.ok(photos);
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long photoId
    ) {
        User currentUser = authService.getCurrentUser(jwt);
        photoService.deletePhoto(currentUser.getId(), photoId);
        return ResponseEntity.noContent().build();
    }
}
