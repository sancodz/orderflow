package com.sathiya.user_service.service;

import com.sathiya.user_service.dto.AuthResponse;
import com.sathiya.user_service.dto.LoginRequest;
import com.sathiya.user_service.dto.SignUpRequest;
import com.sathiya.user_service.entity.Role;
import com.sathiya.user_service.entity.User;
import com.sathiya.user_service.repository.UserRepository;
import com.sathiya.user_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse signup(SignUpRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER) // Default role
                .active(true)
                .build();
        User savedUser = userRepository.save(user);

        String jwtToken = jwtService.generateToken(savedUser);
        return AuthResponse.builder()
                .token(jwtToken)
                .username(savedUser.getUsername())
                .userId(savedUser.getId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        // If authentication is successful, user exists and is active
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found after authentication. This should not happen."));

        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .userId(user.getId())
                .build();
    }

    @Transactional
    public void softDeleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void permanentDeleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        userRepository.delete(user);
    }
}