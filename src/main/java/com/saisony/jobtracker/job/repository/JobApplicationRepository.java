package com.saisony.jobtracker.job.repository;

import com.saisony.jobtracker.job.entity.ApplicationStatus;
import com.saisony.jobtracker.job.entity.JobApplication;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobApplicationRepository
        extends JpaRepository<JobApplication, Long> {

    Optional<JobApplication> findByIdAndUser_Email(
            Long id,
            String email
    );

    Page<JobApplication> findAllByUser_Email(
            String email,
            Pageable pageable
    );

    Page<JobApplication> findAllByUser_EmailAndStatus(
            String email,
            ApplicationStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT j
            FROM JobApplication j
            WHERE j.user.email = :email
              AND (:status IS NULL OR j.status = :status)
              AND (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(j.companyName)
                        LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(j.jobTitle)
                        LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<JobApplication> findFiltered(
            @Param("email") String email,
            @Param("status") ApplicationStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}