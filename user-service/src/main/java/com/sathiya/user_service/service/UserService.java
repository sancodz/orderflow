package com.sathiya.user_service.service;

import com.sathiya.user_service.dto.ProfileUpdateRequest;
import com.sathiya.user_service.dto.UserProfileResponse;
import com.sathiya.user_service.entity.User;
import com.sathiya.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsernameAndActiveTrue(username) // Only active users
                .orElseThrow(() -> new UsernameNotFoundException("User not found or not active: " + username));
        return mapToUserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(String username, ProfileUpdateRequest request) {
        User user = userRepository.findByUsernameAndActiveTrue(username) // Only active users
                .orElseThrow(() -> new UsernameNotFoundException("User not found or not active: " + username));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Check if new email is already in use by another user
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email is already in use by another account.");
            }
            user.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(user);
        return mapToUserProfileResponse(updatedUser);
    }

    private UserProfileResponse mapToUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}