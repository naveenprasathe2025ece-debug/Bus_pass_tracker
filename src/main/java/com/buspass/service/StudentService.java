package com.buspass.service;

import com.buspass.dto.StudentDtos.StudentResponse;
import com.buspass.dto.StudentDtos.UpdateProfileRequest;
import com.buspass.model.Role;
import com.buspass.model.Student;
import com.buspass.repository.StudentRepository;
import com.buspass.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final CurrentUserService currentUserService;

    public StudentService(StudentRepository studentRepository, CurrentUserService currentUserService) {
        this.studentRepository = studentRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public StudentResponse getMyProfile() {
        return StudentResponse.from(currentUserService.getCurrentUser());
    }

    @Transactional
    public StudentResponse updateMyProfile(UpdateProfileRequest request) {
        Student student = currentUserService.getCurrentUser();
        student.setName(request.name().trim());
        student.setDepartment(request.department().trim());
        student.setPhone(request.phone().trim());
        return StudentResponse.from(studentRepository.save(student));
    }

    /** Admin: list every student. */
    @Transactional(readOnly = true)
    public List<StudentResponse> listStudents() {
        return studentRepository.findByRoleOrderByNameAsc(Role.STUDENT).stream()
                .map(StudentResponse::from)
                .toList();
    }
}
