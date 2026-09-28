package com.buspass.repository;

import com.buspass.model.ApplicationStatus;
import com.buspass.model.PassApplication;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PassApplicationRepository extends JpaRepository<PassApplication, Long> {

    @Query("select p from PassApplication p join fetch p.student join fetch p.route "
            + "where p.student.id = :studentId order by p.appliedAt desc")
    List<PassApplication> findAllByStudentWithDetails(@Param("studentId") Long studentId);

    boolean existsByStudentIdAndStatus(Long studentId, ApplicationStatus status);

    /** An "active" pass = APPROVED and validTo is today or later. */
    Optional<PassApplication> findFirstByStudentIdAndStatusAndValidToGreaterThanEqualOrderByValidToDesc(
            Long studentId, ApplicationStatus status, LocalDate date);

    boolean existsByRouteId(Long routeId);

    long countByStatus(ApplicationStatus status);

    long countByStatusAndValidToGreaterThanEqual(ApplicationStatus status, LocalDate date);

    long countByStatusAndValidToBetween(ApplicationStatus status, LocalDate from, LocalDate to);

    @Query("select p from PassApplication p join fetch p.student join fetch p.route "
            + "where (:status is null or p.status = :status) "
            + "and (:routeId is null or p.route.id = :routeId) "
            + "and (:fromTs is null or p.appliedAt >= :fromTs) "
            + "and (:toTs is null or p.appliedAt < :toTs) "
            + "order by p.appliedAt desc")
    List<PassApplication> search(@Param("status") ApplicationStatus status,
                                 @Param("routeId") Long routeId,
                                 @Param("fromTs") LocalDateTime fromTs,
                                 @Param("toTs") LocalDateTime toTs);

    @Query("select p from PassApplication p join fetch p.student join fetch p.route "
            + "where p.status = :status and p.validTo between :from and :to order by p.validTo asc")
    List<PassApplication> findExpiring(@Param("status") ApplicationStatus status,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to);

    /** Row lock so two admins cannot review the same application at the same time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PassApplication p where p.id = :id")
    Optional<PassApplication> findByIdForUpdate(@Param("id") Long id);
}
