package com.buspass.controller;

import com.buspass.dto.StudentDtos.StudentResponse;
import com.buspass.dto.StudentDtos.UpdateProfileRequest;
import com.buspass.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/me")
    public StudentResponse me() {
        return studentService.getMyProfile();
    }

    @PutMapping("/me")
    public StudentResponse updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return studentService.updateMyProfile(request);
    }
}
