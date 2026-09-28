package com.buspass.dto;

import com.buspass.model.ApplicationStatus;
import com.buspass.model.PassApplication;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class ApplicationDtos {

    private ApplicationDtos() {
    }

    /** Used by a student to create or edit an application (route, boarding point, photo reference). */
    public record ApplicationRequest(
            @NotNull(message = "Route is required")
            @Positive(message = "Route id must be positive")
            Long routeId,

            @NotBlank(message = "Boarding point is required")
            @Size(max = 100, message = "Boarding point must be at most 100 characters")
            String boardingPoint,

            @NotBlank(message = "Photo reference is required")
            @Size(max = 255, message = "Photo reference must be at most 255 characters")
            String photoReference) {
    }

    /** Admin approval: remark plus the validity period chosen by the admin. */
    public record ApproveRequest(
            @NotBlank(message = "Remark is required")
            @Size(max = 500, message = "Remark must be at most 500 characters")
            String remark,

            @NotNull(message = "Valid-from date is required")
            LocalDate validFrom,

            @NotNull(message = "Valid-to date is required")
            LocalDate validTo) {
    }

    /** Admin rejection: remark plus a mandatory rejection reason. */
    public record RejectRequest(
            @NotBlank(message = "Remark is required")
            @Size(max = 500, message = "Remark must be at most 500 characters")
            String remark,

            @NotBlank(message = "Rejection reason is required")
            @Size(max = 500, message = "Rejection reason must be at most 500 characters")
            String rejectionReason) {
    }

    public record ApplicationResponse(
            Long id,
            Long studentId,
            String studentName,
            String registerNumber,
            Long routeId,
            String routeNumber,
            String routeName,
            String boardingPoint,
            String photoReference,
            ApplicationStatus status,
            String remark,
            String rejectionReason,
            String passNumber,
            LocalDate validFrom,
            LocalDate validTo,
            String passState,
            Long daysToExpiry,
            LocalDateTime appliedAt,
            LocalDateTime reviewedAt) {

        public static ApplicationResponse from(PassApplication p) {
            LocalDate today = LocalDate.now();
            String passState = null;
            Long daysToExpiry = null;
            if (p.getStatus() == ApplicationStatus.APPROVED && p.getValidTo() != null) {
                passState = p.getValidTo().isBefore(today) ? "EXPIRED" : "ACTIVE";
                daysToExpiry = ChronoUnit.DAYS.between(today, p.getValidTo());
            }
            return new ApplicationResponse(
                    p.getId(),
                    p.getStudent().getId(),
                    p.getStudent().getName(),
                    p.getStudent().getRegisterNumber(),
                    p.getRoute().getId(),
                    p.getRoute().getRouteNumber(),
                    p.getRoute().getRouteName(),
                    p.getBoardingPoint(),
                    p.getPhotoReference(),
                    p.getStatus(),
                    p.getRemark(),
                    p.getRejectionReason(),
                    p.getPassNumber(),
                    p.getValidFrom(),
                    p.getValidTo(),
                    passState,
                    daysToExpiry,
                    p.getAppliedAt(),
                    p.getReviewedAt());
        }
    }

    public record DashboardResponse(long totalStudents, long totalRoutes, long totalApplications,
                                    long pendingApplications, long approvedApplications,
                                    long rejectedApplications, long activePasses,
                                    long passesExpiringIn30Days) {
    }
}
