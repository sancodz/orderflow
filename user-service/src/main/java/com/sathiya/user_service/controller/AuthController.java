package com.sathiya.user_service.controller;

import com.sathiya.user_service.dto.AuthResponse;
import com.sathiya.user_service.dto.LoginRequest;
import com.sathiya.user_service.dto.SignUpRequest;
import com.sathiya.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(signUpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/logout") // Client-side logout
    public ResponseEntity<String> logout() {
        // For JWT, logout is typically handled client-side by deleting the token.
        // This endpoint can be a confirmation or used if server-side token blacklisting is implemented.
        SecurityContextHolder.clearContext(); // Optional: clear context on server side
        return ResponseEntity.ok("Logged out successfully. Please clear your token on the client-side.");
    }

    @DeleteMapping("/user") // Soft delete
    public ResponseEntity<String> deactivateUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        authService.softDeleteUser(principal.getName());
        return ResponseEntity.ok("User account deactivated successfully.");
    }

    @DeleteMapping("/user/permanent") // Permanent delete
    public ResponseEntity<String> deleteUserPermanently(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        authService.permanentDeleteUser(principal.getName());
        return ResponseEntity.ok("User account permanently deleted successfully.");
    }
}