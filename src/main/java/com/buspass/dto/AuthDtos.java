package com.buspass.dto;

import com.buspass.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Name is required")
            @Size(max = 100, message = "Name must be at most 100 characters")
            String name,

            @NotBlank(message = "Email is required")
            @Email(message = "Email format is invalid")
            @Size(max = 150, message = "Email must be at most 150 characters")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 6, max = 64, message = "Password must be 6 to 64 characters")
            String password,

            @NotBlank(message = "Register number is required")
            @Size(max = 30, message = "Register number must be at most 30 characters")
            String registerNumber,

            @NotBlank(message = "Department is required")
            @Size(max = 100, message = "Department must be at most 100 characters")
            String department,

            @NotBlank(message = "Phone is required")
            @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit mobile number")
            String phone) {
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email format is invalid")
            String email,

            @NotBlank(message = "Password is required")
            String password) {
    }

    public record AuthResponse(String token, String tokenType, Long userId,
                               String name, String email, Role role) {
    }
}
