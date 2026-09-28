package com.buspass.service;

import com.buspass.dto.ApplicationDtos.DashboardResponse;
import com.buspass.model.ApplicationStatus;
import com.buspass.model.Role;
import com.buspass.repository.BusRouteRepository;
import com.buspass.repository.PassApplicationRepository;
import com.buspass.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Simple counts for the admin dashboard. */
@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final BusRouteRepository routeRepository;
    private final PassApplicationRepository applicationRepository;

    public DashboardService(StudentRepository studentRepository, BusRouteRepository routeRepository,
                            PassApplicationRepository applicationRepository) {
        this.studentRepository = studentRepository;
        this.routeRepository = routeRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        return new DashboardResponse(
                studentRepository.countByRole(Role.STUDENT),
                routeRepository.count(),
                applicationRepository.count(),
                applicationRepository.countByStatus(ApplicationStatus.PENDING),
                applicationRepository.countByStatus(ApplicationStatus.APPROVED),
                applicationRepository.countByStatus(ApplicationStatus.REJECTED),
                applicationRepository.countByStatusAndValidToGreaterThanEqual(ApplicationStatus.APPROVED, today),
                applicationRepository.countByStatusAndValidToBetween(ApplicationStatus.APPROVED, today, today.plusDays(30)));
    }
}
