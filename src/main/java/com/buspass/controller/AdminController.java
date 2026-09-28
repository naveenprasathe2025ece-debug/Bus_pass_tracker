package com.buspass.controller;

import com.buspass.dto.ApplicationDtos.ApplicationResponse;
import com.buspass.dto.ApplicationDtos.ApproveRequest;
import com.buspass.dto.ApplicationDtos.DashboardResponse;
import com.buspass.dto.ApplicationDtos.RejectRequest;
import com.buspass.dto.StudentDtos.StudentResponse;
import com.buspass.model.ApplicationStatus;
import com.buspass.service.DashboardService;
import com.buspass.service.PassApplicationService;
import com.buspass.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Admin-only endpoints (role ADMIN). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PassApplicationService applicationService;
    private final DashboardService dashboardService;
    private final StudentService studentService;

    public AdminController(PassApplicationService applicationService, DashboardService dashboardService,
                           StudentService studentService) {
        this.applicationService = applicationService;
        this.dashboardService = dashboardService;
        this.studentService = studentService;
    }

    /** List / filter applications by status, route and applied date range. */
    @GetMapping("/applications")
    public List<ApplicationResponse> listApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appliedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appliedTo) {
        return applicationService.search(status, routeId, appliedFrom, appliedTo);
    }

    @GetMapping("/applications/{id}")
    public ApplicationResponse getApplication(@PathVariable Long id) {
        return applicationService.getById(id);
    }

    /** Feature 2 + 3: approve with remark, validity period and generated pass number. */
    @PutMapping("/applications/{id}/approve")
    public ApplicationResponse approve(@PathVariable Long id, @Valid @RequestBody ApproveRequest request) {
        return applicationService.approve(id, request);
    }

    /** Feature 2: reject with remark and mandatory rejection reason. */
    @PutMapping("/applications/{id}/reject")
    public ApplicationResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return applicationService.reject(id, request);
    }

    /** Feature 5: passes expiring in the next N days (default 30). */
    @GetMapping("/passes/expiring")
    public List<ApplicationResponse> expiring(@RequestParam(defaultValue = "30") int days) {
        return applicationService.expiringPasses(days);
    }

    /** Extra: dashboard counts. */
    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return dashboardService.getDashboard();
    }

    @GetMapping("/students")
    public List<StudentResponse> students() {
        return studentService.listStudents();
    }
}
