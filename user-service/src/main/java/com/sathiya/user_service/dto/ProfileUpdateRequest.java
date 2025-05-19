package com.sathiya.user_service.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    // Email can be updated, but ensure uniqueness checks if it is
    @Email(message = "Email should be valid if provided")
    private String email;
    private String firstName;
    private String lastName;
}