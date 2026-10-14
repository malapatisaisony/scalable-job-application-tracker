package com.saisony.jobtracker.job.service;

import com.saisony.jobtracker.exception.ResourceNotFoundException;
import com.saisony.jobtracker.job.dto.CreateJobApplicationRequest;
import com.saisony.jobtracker.job.dto.JobApplicationResponse;
import com.saisony.jobtracker.job.dto.UpdateJobApplicationRequest;
import com.saisony.jobtracker.job.entity.ApplicationStatus;
import com.saisony.jobtracker.job.entity.JobApplication;
import com.saisony.jobtracker.job.repository.JobApplicationRepository;
import com.saisony.jobtracker.user.entity.User;
import com.saisony.jobtracker.user.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;

    public JobApplicationService(
            JobApplicationRepository jobApplicationRepository,
            UserRepository userRepository) {

        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public JobApplicationResponse create(
            String userEmail,
            CreateJobApplicationRequest request) {

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        JobApplication application = new JobApplication();

        application.setUser(user);
        application.setCompanyName(request.getCompanyName());
        application.setJobTitle(request.getJobTitle());
        application.setJobUrl(request.getJobUrl());
        application.setLocation(request.getLocation());
        application.setAppliedAt(request.getAppliedAt());

        if (request.getStatus() != null) {
            application.setStatus(request.getStatus());
        } else {
            application.setStatus(ApplicationStatus.SAVED);
        }

        JobApplication saved =
                jobApplicationRepository.saveAndFlush(application);

        return new JobApplicationResponse(
                saved.getId(),
                saved.getCompanyName(),
                saved.getJobTitle(),
                saved.getJobUrl(),
                saved.getLocation(),
                saved.getStatus(),
                saved.getAppliedAt(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "jobApplications",
            key = "#userEmail + ':' + #id"
    )
    public JobApplicationResponse getById(
            Long id,
            String userEmail) {

        JobApplication application = jobApplicationRepository
                .findByIdAndUser_Email(id, userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Job application not found"
                        )
                );

        return new JobApplicationResponse(
                application.getId(),
                application.getCompanyName(),
                application.getJobTitle(),
                application.getJobUrl(),
                application.getLocation(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }

    @Transactional
    @CacheEvict(
            value = "jobApplications",
            key = "#userEmail + ':' + #id"
    )
    public JobApplicationResponse update(
            Long id,
            String userEmail,
            UpdateJobApplicationRequest request) {

        JobApplication application = jobApplicationRepository
                .findByIdAndUser_Email(id, userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Job application not found"
                        )
                );

        if (request.getCompanyName() != null) {
            application.setCompanyName(request.getCompanyName());
        }

        if (request.getJobTitle() != null) {
            application.setJobTitle(request.getJobTitle());
        }

        if (request.getJobUrl() != null) {
            application.setJobUrl(request.getJobUrl());
        }

        if (request.getLocation() != null) {
            application.setLocation(request.getLocation());
        }

        if (request.getStatus() != null) {
            application.setStatus(request.getStatus());
        }

        if (request.getAppliedAt() != null) {
            application.setAppliedAt(request.getAppliedAt());
        }

        JobApplication saved =
                jobApplicationRepository.saveAndFlush(application);

        return new JobApplicationResponse(
                saved.getId(),
                saved.getCompanyName(),
                saved.getJobTitle(),
                saved.getJobUrl(),
                saved.getLocation(),
                saved.getStatus(),
                saved.getAppliedAt(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    @Transactional
    @CacheEvict(
            value = "jobApplications",
            key = "#userEmail + ':' + #id"
    )
    public void delete(
            Long id,
            String userEmail) {

        JobApplication application = jobApplicationRepository
                .findByIdAndUser_Email(id, userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Job application not found"
                        )
                );

        jobApplicationRepository.delete(application);
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getAllForUserPaginated(
            String userEmail,
            ApplicationStatus status,
            String search,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<JobApplication> applications =
                jobApplicationRepository.findFiltered(
                        userEmail,
                        status,
                        search,
                        pageable
                );

        return applications.map(application ->
                new JobApplicationResponse(
                        application.getId(),
                        application.getCompanyName(),
                        application.getJobTitle(),
                        application.getJobUrl(),
                        application.getLocation(),
                        application.getStatus(),
                        application.getAppliedAt(),
                        application.getCreatedAt(),
                        application.getUpdatedAt()
                )
        );
    }
}