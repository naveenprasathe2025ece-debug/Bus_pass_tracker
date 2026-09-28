package com.buspass.security;

import com.buspass.model.Student;
import com.buspass.repository.StudentRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Resolves the logged-in user from the JWT-based security context. */
@Service
public class CurrentUserService {

    private final StudentRepository studentRepository;

    public CurrentUserService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new BadCredentialsException("Not authenticated");
        }
        return studentRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new BadCredentialsException("User no longer exists"));
    }
}
