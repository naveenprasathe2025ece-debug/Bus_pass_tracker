package com.buspass.controller;

import com.buspass.dto.ApplicationDtos.ApplicationRequest;
import com.buspass.dto.ApplicationDtos.ApplicationResponse;
import com.buspass.service.PassApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Student-facing endpoints (role STUDENT). */
@RestController
@RequestMapping("/api/applications")
public class PassApplicationController {

    private final PassApplicationService applicationService;

    public PassApplicationController(PassApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /** Feature 1: submit an application. */
    @PostMapping
    public ResponseEntity<ApplicationResponse> apply(@Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.apply(request));
    }

    /** Feature 4: view my application status at any time. */
    @GetMapping("/my")
    public List<ApplicationResponse> myApplications() {
        return applicationService.myApplications();
    }

    @GetMapping("/{id}")
    public ApplicationResponse getMine(@PathVariable Long id) {
        return applicationService.getMine(id);
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable Long id, @Valid @RequestBody ApplicationRequest request) {
        return applicationService.updateMine(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        applicationService.cancelMine(id);
        return ResponseEntity.noContent().build();
    }
}
