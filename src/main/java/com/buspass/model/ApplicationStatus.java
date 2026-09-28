package com.buspass.model;

/**
 * Stored status of an application. "Expired" is not stored: an APPROVED pass whose
 * validTo date is in the past is treated as expired (computed from the date).
 */
public enum ApplicationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
