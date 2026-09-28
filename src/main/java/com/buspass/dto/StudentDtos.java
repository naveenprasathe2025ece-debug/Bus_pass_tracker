package com.buspass.dto;

import com.buspass.model.Role;
import com.buspass.model.Student;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class StudentDtos {

    private StudentDtos() {
    }

    public record UpdateProfileRequest(
            @NotBlank(message = "Name is required")
            @Size(max = 100, message = "Name must be at most 100 characters")
            String name,

            @NotBlank(message = "Department is required")
            @Size(max = 100, message = "Department must be at most 100 characters")
            String department,

            @NotBlank(message = "Phone is required")
            @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit mobile number")
            String phone) {
    }

    public record StudentResponse(Long id, String name, String email, String registerNumber,
                                  String department, String phone, Role role, LocalDateTime createdAt) {

        public static StudentResponse from(Student s) {
            return new StudentResponse(s.getId(), s.getName(), s.getEmail(), s.getRegisterNumber(),
                    s.getDepartment(), s.getPhone(), s.getRole(), s.getCreatedAt());
        }
    }
}
