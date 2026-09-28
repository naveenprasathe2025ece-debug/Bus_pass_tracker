package com.buspass.service;

import com.buspass.dto.ApplicationDtos.ApplicationRequest;
import com.buspass.dto.ApplicationDtos.ApplicationResponse;
import com.buspass.dto.ApplicationDtos.ApproveRequest;
import com.buspass.dto.ApplicationDtos.RejectRequest;
import com.buspass.exception.BusinessRuleException;
import com.buspass.exception.ResourceNotFoundException;
import com.buspass.model.ApplicationStatus;
import com.buspass.model.BusRoute;
import com.buspass.model.PassApplication;
import com.buspass.model.Student;
import com.buspass.repository.BusRouteRepository;
import com.buspass.repository.PassApplicationRepository;
import com.buspass.repository.StudentRepository;
import com.buspass.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * All business rules of the BusPass system live here (service layer), so an invalid
 * request is rejected immediately with a clear message and never reaches the database.
 *
 * Rule 1: a student can have only one active pass at a time.
 * Rule 2: a rejected application must include a rejection reason before it is saved.
 */
@Service
public class PassApplicationService {

    private final PassApplicationRepository applicationRepository;
    private final BusRouteRepository routeRepository;
    private final StudentRepository studentRepository;
    private final CurrentUserService currentUserService;

    public PassApplicationService(PassApplicationRepository applicationRepository,
                                  BusRouteRepository routeRepository,
                                  StudentRepository studentRepository,
                                  CurrentUserService currentUserService) {
        this.applicationRepository = applicationRepository;
        this.routeRepository = routeRepository;
        this.studentRepository = studentRepository;
        this.currentUserService = currentUserService;
    }

    // ------------------------------------------------------------------ student side

    /** Feature 1: student submits a pass application (route, boarding point, photo reference). */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApplicationResponse apply(ApplicationRequest request) {
        Student current = currentUserService.getCurrentUser();
        // lock the student row so two parallel requests cannot both pass the checks below
        Student student = studentRepository.findByIdForUpdate(current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        BusRoute route = loadActiveRoute(request.routeId());

        ensureNoActivePass(student.getId(), LocalDate.now());               // Rule 1
        if (applicationRepository.existsByStudentIdAndStatus(student.getId(), ApplicationStatus.PENDING)) {
            throw new BusinessRuleException(
                    "You already have a pending application. Wait for it to be reviewed, or edit/cancel it.");
        }

        PassApplication app = new PassApplication();
        app.setStudent(student);
        app.setRoute(route);
        app.setBoardingPoint(request.boardingPoint().trim());
        app.setPhotoReference(request.photoReference().trim());
        app.setStatus(ApplicationStatus.PENDING);
        return ApplicationResponse.from(applicationRepository.save(app));
    }

    /** Feature 4: student views the status of all of their applications. */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> myApplications() {
        Student student = currentUserService.getCurrentUser();
        return applicationRepository.findAllByStudentWithDetails(student.getId()).stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getMine(Long id) {
        return ApplicationResponse.from(findOwned(id));
    }

    /** A student may edit an application only while it is still PENDING. */
    @Transactional
    public ApplicationResponse updateMine(Long id, ApplicationRequest request) {
        PassApplication app = findOwned(id);
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING applications can be edited.");
        }
        app.setRoute(loadActiveRoute(request.routeId()));
        app.setBoardingPoint(request.boardingPoint().trim());
        app.setPhotoReference(request.photoReference().trim());
        return ApplicationResponse.from(applicationRepository.save(app));
    }

    /** A student may cancel (delete) an application only while it is still PENDING. */
    @Transactional
    public void cancelMine(Long id) {
        PassApplication app = findOwned(id);
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING applications can be cancelled.");
        }
        applicationRepository.delete(app);
    }

    // ------------------------------------------------------------------ admin side

    @Transactional(readOnly = true)
    public List<ApplicationResponse> search(ApplicationStatus status, Long routeId,
                                            LocalDate appliedFrom, LocalDate appliedTo) {
        if (appliedFrom != null && appliedTo != null && appliedTo.isBefore(appliedFrom)) {
            throw new BusinessRuleException("appliedTo cannot be before appliedFrom.");
        }
        LocalDateTime from = appliedFrom == null ? null : appliedFrom.atStartOfDay();
        LocalDateTime to = appliedTo == null ? null : appliedTo.plusDays(1).atStartOfDay();
        return applicationRepository.search(status, routeId, from, to).stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getById(Long id) {
        return ApplicationResponse.from(applicationRepository.findById(id)
                .orElseThrow(() -> notFound(id)));
    }

    /** Feature 2 + 3: admin approves with a remark; validity period and pass number are set. */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApplicationResponse approve(Long id, ApproveRequest request) {
        LocalDate today = LocalDate.now();
        PassApplication app = applicationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> notFound(id));
        requirePending(app);

        if (!request.validTo().isAfter(request.validFrom())) {
            throw new BusinessRuleException("Valid-to date must be after the valid-from date.");
        }
        if (request.validTo().isBefore(today)) {
            throw new BusinessRuleException("Valid-to date cannot be in the past.");
        }

        Student student = studentRepository.findByIdForUpdate(app.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        ensureNoActivePass(student.getId(), today);                         // Rule 1 (re-checked at approval)

        app.setStatus(ApplicationStatus.APPROVED);
        app.setRemark(request.remark().trim());
        app.setRejectionReason(null);
        app.setValidFrom(request.validFrom());
        app.setValidTo(request.validTo());
        app.setPassNumber(generatePassNumber(app.getId(), today));
        app.setReviewedAt(LocalDateTime.now());
        return ApplicationResponse.from(applicationRepository.save(app));
    }

    /** Feature 2: admin rejects with a remark; a rejection reason is mandatory (Rule 2). */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApplicationResponse reject(Long id, RejectRequest request) {
        // Rule 2 is enforced here in the service layer, before anything is saved
        if (request.rejectionReason() == null || request.rejectionReason().isBlank()) {
            throw new BusinessRuleException("A rejection reason is required to reject an application.");
        }
        PassApplication app = applicationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> notFound(id));
        requirePending(app);

        app.setStatus(ApplicationStatus.REJECTED);
        app.setRemark(request.remark().trim());
        app.setRejectionReason(request.rejectionReason().trim());
        app.setValidFrom(null);
        app.setValidTo(null);
        app.setPassNumber(null);
        app.setReviewedAt(LocalDateTime.now());
        return ApplicationResponse.from(applicationRepository.save(app));
    }

    /** Feature 5: admin lists passes expiring in the next N days (default 30). */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> expiringPasses(int days) {
        if (days < 1 || days > 365) {
            throw new BusinessRuleException("days must be between 1 and 365.");
        }
        LocalDate today = LocalDate.now();
        return applicationRepository.findExpiring(ApplicationStatus.APPROVED, today, today.plusDays(days)).stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    // ------------------------------------------------------------------ helpers

    /** Rule 1: throws when the student already holds an APPROVED pass that has not yet expired. */
    private void ensureNoActivePass(Long studentId, LocalDate today) {
        applicationRepository
                .findFirstByStudentIdAndStatusAndValidToGreaterThanEqualOrderByValidToDesc(
                        studentId, ApplicationStatus.APPROVED, today)
                .ifPresent(active -> {
                    throw new BusinessRuleException("A student can have only one active pass at a time. "
                            + "Pass " + active.getPassNumber() + " is valid until " + active.getValidTo() + ".");
                });
    }

    private void requirePending(PassApplication app) {
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessRuleException("Application " + app.getId() + " is already "
                    + app.getStatus() + " and cannot be reviewed again.");
        }
    }

    private BusRoute loadActiveRoute(Long routeId) {
        BusRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id " + routeId));
        if (!route.isActive()) {
            throw new BusinessRuleException("Route " + route.getRouteNumber() + " is not accepting applications.");
        }
        return route;
    }

    private PassApplication findOwned(Long id) {
        Student current = currentUserService.getCurrentUser();
        PassApplication app = applicationRepository.findById(id).orElseThrow(() -> notFound(id));
        if (!app.getStudent().getId().equals(current.getId())) {
            throw notFound(id);   // do not reveal that another student's application exists
        }
        return app;
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Application not found with id " + id);
    }

    /** Example: BP2026000042  (BP + year + application id padded to 6 digits) - unique per application. */
    private String generatePassNumber(Long applicationId, LocalDate today) {
        return String.format("BP%d%06d", today.getYear(), applicationId);
    }
}
