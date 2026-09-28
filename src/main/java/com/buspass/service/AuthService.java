package com.buspass.service;

import com.buspass.dto.AuthDtos.AuthResponse;
import com.buspass.dto.AuthDtos.LoginRequest;
import com.buspass.dto.AuthDtos.RegisterRequest;
import com.buspass.exception.BusinessRuleException;
import com.buspass.model.Role;
import com.buspass.model.Student;
import com.buspass.repository.StudentRepository;
import com.buspass.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(StudentRepository studentRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String registerNumber = request.registerNumber().trim().toUpperCase();

        if (studentRepository.existsByEmail(email)) {
            throw new BusinessRuleException("This email is already registered.");
        }
        if (studentRepository.existsByRegisterNumber(registerNumber)) {
            throw new BusinessRuleException("This register number is already registered.");
        }

        Student student = new Student();
        student.setName(request.name().trim());
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode(request.password()));
        student.setRegisterNumber(registerNumber);
        student.setDepartment(request.department().trim());
        student.setPhone(request.phone().trim());
        student.setRole(Role.STUDENT);
        return toAuthResponse(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return toAuthResponse(student);
    }

    private AuthResponse toAuthResponse(Student s) {
        String token = jwtUtil.generateToken(s.getEmail(), s.getRole().name());
        return new AuthResponse(token, "Bearer", s.getId(), s.getName(), s.getEmail(), s.getRole());
    }
}
