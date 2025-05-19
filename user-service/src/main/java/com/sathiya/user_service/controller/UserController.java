package com.sathiya.user_service.controller;

import com.sathiya.user_service.dto.ProfileUpdateRequest;
import com.sathiya.user_service.dto.UserProfileResponse;
import com.sathiya.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Or throw exception
        }
        return ResponseEntity.ok(userService.getUserProfile(principal.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(Principal principal,
                                                                 @Valid @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Or throw exception
        }
        return ResponseEntity.ok(userService.updateUserProfile(principal.getName(), profileUpdateRequest));
    }
}